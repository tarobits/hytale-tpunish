package dev.tarobits.punishments.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.storage.StorageUtils;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PunishmentProvider extends BlockingDiskFile {
    private static PunishmentProvider INSTANCE;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Map<PunishmentType, Integer> stats = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<Punishment>> punishments = new Object2ObjectOpenHashMap<>();

    // Indexes
    private final Map<UUID, Punishment> activeBans = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Punishment> activeMutes = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<Punishment>> warns = new Object2ObjectOpenHashMap<>();

    public PunishmentProvider() {
        Path pluginDir = TPunish.getInstance().getDataDirectory();
        super(StorageUtils.createDataFile(pluginDir, "punishments.json").toPath());
        this.syncLoad();
        String loadedString = "Successfully loaded " + this.stats.computeIfAbsent(PunishmentType.BAN, (_) -> 0) + " bans, " +
                this.stats.computeIfAbsent(PunishmentType.MUTE, (_) -> 0) + " mutes, " +
                this.stats.computeIfAbsent(PunishmentType.KICK, (_) -> 0) + " kicks and " +
                this.stats.computeIfAbsent(PunishmentType.WARN, (_) -> 0) + " warnings!";
        LOGGER.atInfo().log(loadedString);
        INSTANCE = this;
    }

    public static PunishmentProvider get() {
        if (INSTANCE == null) {
            INSTANCE = new PunishmentProvider();
        }
        return INSTANCE;
    }

    public Integer getNewId() {
        return this.punishments.size();
    }

    @Nullable
    public String userHasPunishment(UUID uuid, PunishmentType type) {
        return switch(type) {
            case BAN -> activeBans.containsKey(uuid) ? "ban" : null;
            case MUTE -> activeMutes.containsKey(uuid) ? "mute": null;
            case KICK, WARN -> null;
        };
    }

    public void addPunishment(Punishment punishment) {
        this.punishments.computeIfAbsent(punishment.getTarget(), _ -> new ArrayList<>())
                .add(punishment);

        this.updateIndexes();

        this.syncSave();
    }

    public void updateIndexes() {
        this.activeBans.clear();
        this.activeMutes.clear();
        this.warns.clear();
        this.punishments.forEach((_,p) -> {
            for (Punishment punishment : p) {
                if (punishment.isActive() && !punishment.isPardoned()) {
                    switch (punishment.getType()) {
                        case BAN -> this.activeBans.put(punishment.getTarget(), punishment);
                        case MUTE -> this.activeMutes.put(punishment.getTarget(), punishment);
                        case WARN -> this.warns.computeIfAbsent(punishment.getTarget(), _ -> new ArrayList<>())
                                .add(punishment);
                    }
                }
            }
        });
    }

    public Punishment getActive(UUID uuid, PunishmentType type) {
        return switch (type) {
            case BAN -> this.activeBans.get(uuid);
            case MUTE -> this.activeMutes.get(uuid);
            default -> throw new IllegalArgumentException("Type does not have active entries!");
        };
    }

    public List<Punishment> getEntries(UUID uuid, PunishmentType type) {
        List<Punishment> punishmentList = new ArrayList<>();

        for (Punishment p : this.punishments.computeIfAbsent(uuid, _ -> new ArrayList<>())) {
            if (p.getType() == type) {
                punishmentList.add(p);
            }
        }
        return punishmentList;
    }

    public Boolean hasBan(UUID uuid) {
        return this.activeBans.get(uuid) != null;
    }

    public Boolean hasMute(UUID uuid) {
        return this.activeMutes.get(uuid) != null;
    }

    public Integer findPunishment(UUID uuid, PunishmentType type, Integer id) {
        if (switch (type) {
            case BAN -> activeBans.get(uuid).getHistoryId().equals(id) ? true : null;
            case MUTE -> activeMutes.get(uuid).getHistoryId().equals(id) ? true : null;
            case WARN -> warns.get(uuid).stream().filter((p) -> p.getHistoryId().equals(id)).findFirst().orElse(null);
            default -> punishments.get(uuid).stream().filter((p) -> p.getHistoryId().equals(id)).findFirst().orElse(null);
        } == null) {
            return -1;
        }
        int i = 0;
        for (Punishment p : punishments.computeIfAbsent(uuid, _ -> new ArrayList<>())) {
            if (p.getHistoryId().equals(id)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public Boolean editPunishment(UUID uuid, PunishmentType type, Integer id, Function<Punishment, Boolean> modifier) {
        Integer index = this.findPunishment(uuid, type, id);
        if (index == -1) {
            return false;
        }
        return modifier.apply(this.punishments.get(uuid).get(index));
    }

    public Message updatePunishment(Punishment punishment, Integer id) {
        Integer index = this.findPunishment(punishment.getTarget(),punishment.getType(),id);
        if (index == -1) {
            return switch (punishment.getType()) {
                case BAN, MUTE -> Message.translation("tarobits.punishments.edit.error.onlyactive");
                default -> Message.translation("tarobits.punishments.edit.error.notfound");
            };
        }
        this.punishments.get(punishment.getTarget()).set(index, punishment);
        this.updateIndexes();
        // ToDo: Add Logs
        this.syncSave();
        return Message.translation("tarobits.punishments.edit.success");
    }

    @Override
    protected void read(BufferedReader bufferedReader) {
        JsonParser.parseReader(bufferedReader).getAsJsonArray().forEach((entry) -> {
            JsonObject jsonObject = entry.getAsJsonObject();

            try {
                Punishment punishment = Punishment.fromJson(jsonObject);
                this.stats.compute(punishment.getType(), (k,v) -> v == null ? 1 : v++);
                this.addPunishment(punishment);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to parse!", ex);
            }
        });
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            jsonWriter.beginArray().endArray();
        }
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonArray array = new JsonArray();
        this.punishments.forEach((_, value) ->
                value.forEach((k) -> array.add(k.toJsonObject())));
        bufferedWriter.write(array.toString());
    }
}
