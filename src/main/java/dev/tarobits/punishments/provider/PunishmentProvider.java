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
import dev.tarobits.punishments.utils.ProviderState;
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

    private ProviderState STATE = ProviderState.INIT;

    private final Map<PunishmentType, Integer> stats = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<Punishment>> userPunishments = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Punishment> punishments = new Object2ObjectOpenHashMap<>();

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

    @Nullable
    public String userHasPunishment(UUID user, PunishmentType type) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        return switch (type) {
            case BAN -> activeBans.containsKey(user) ? "ban" : null;
            case MUTE -> activeMutes.containsKey(user) ? "mute" : null;
            case KICK, WARN -> null;
        };
    }

    private Punishment ensureUniqueId(Punishment punishment) {
        UUID id = punishment.getId();
        if (!this.punishments.containsKey(id)) {
            return punishment;
        }

        LOGGER.atWarning().log(
                "Punishment id " + punishment.getId() + " already exists! Giving new id!"
        );

        return punishment.withId(UUID.randomUUID());
    }

    private Boolean canFileAction() {
        return this.STATE == ProviderState.INIT || this.STATE == ProviderState.READY;
    }

    private Boolean isReading() {
        return this.STATE == ProviderState.READ;
    }
    private Boolean isReady() {
        return this.STATE == ProviderState.READY;
    }

    public void addPunishment(Punishment punishment) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        this.punishments.put(punishment.getId(), punishment);

        // ToDo: Add logging

        this.updateIndexes();

        this.syncSave();
    }

    private void addFilePunishment(Punishment punishment) {
        if (!this.isReading()) throw new IllegalArgumentException("Punishment Provider cannot add FilePunishment if not reading!");
        this.punishments.put(punishment.getId(), punishment);
    }

    public void updateIndexes() {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        this.STATE = ProviderState.INDEX;
        this.activeBans.clear();
        this.activeMutes.clear();
        this.warns.clear();
        this.userPunishments.clear();
        this.punishments.forEach((_, punishment) -> {
            this.userPunishments.compute(punishment.getTarget(), (_, l) -> {
                List<Punishment> list = l;
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(punishment);
                return list;
            });
            if (punishment.isActive() && !punishment.isPardoned()) {
                switch (punishment.getType()) {
                    case BAN -> this.activeBans.put(punishment.getTarget(), punishment);
                    case MUTE -> this.activeMutes.put(punishment.getTarget(), punishment);
                    case WARN -> this.warns.computeIfAbsent(punishment.getTarget(), _ -> new ArrayList<>())
                            .add(punishment);
                }
            }
        });
        this.STATE = ProviderState.READY;
    }

    public Punishment getActive(UUID user, PunishmentType type) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        return switch (type) {
            case BAN -> this.activeBans.get(user);
            case MUTE -> this.activeMutes.get(user);
            default -> throw new IllegalArgumentException("Type does not have active entries!");
        };
    }

    public List<Punishment> getEntries(UUID user, PunishmentType type) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        List<Punishment> punishmentList = new ArrayList<>();

        for (Punishment p : this.userPunishments.computeIfAbsent(user, _ -> new ArrayList<>())) {
            if (p.getType() == type) {
                punishmentList.add(p);
            }
        }
        return punishmentList;
    }

    public Boolean hasBan(UUID user) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        return this.activeBans.get(user) != null;
    }

    public Boolean hasMute(UUID user) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        return this.activeMutes.get(user) != null;
    }

    public Integer findPunishment(UUID user, PunishmentType type, UUID id) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        if (switch (type) {
            case BAN -> activeBans.get(user).getId().equals(id) ? true : null;
            case MUTE -> activeMutes.get(user).getId().equals(id) ? true : null;
            case WARN -> warns.get(user).stream().filter((p) -> p.getId().equals(id)).findFirst().orElse(null);
            default -> userPunishments.get(user).stream().filter((p) -> p.getId().equals(id)).findFirst().orElse(null);
        } == null) {
            return -1;
        }
        int i = 0;
        for (Punishment p : userPunishments.computeIfAbsent(user, _ -> new ArrayList<>())) {
            if (p.getId().equals(id)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public Boolean editPunishment(UUID user, PunishmentType type, UUID id, Function<Punishment, Boolean> modifier) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        Integer index = this.findPunishment(user, type, id);
        if (index == -1) {
            return false;
        }
        Boolean res = modifier.apply(this.userPunishments.get(user).get(index));
        this.syncSave();
        this.updateIndexes();
        return res;
    }

    public Message updatePunishment(Punishment punishment, UUID id) {
        if (!this.isReady()) throw new IllegalArgumentException("Punishment Provider is not ready!");
        Integer index = this.findPunishment(punishment.getTarget(), punishment.getType(), id);
        if (index == -1) {
            return switch (punishment.getType()) {
                case BAN, MUTE -> Message.translation("tarobits.punishments.edit.error.onlyactive");
                default -> Message.translation("tarobits.punishments.edit.error.notfound");
            };
        }
        this.userPunishments.get(punishment.getTarget()).set(index, punishment);
        this.updateIndexes();
        // ToDo: Add Logs
        this.syncSave();
        return Message.translation("tarobits.punishments.edit.success");
    }

    @Override
    protected void read(BufferedReader bufferedReader) {
        if (!this.canFileAction()) throw new IllegalArgumentException("Punishment provider cannot read!");
        this.STATE = ProviderState.READ;
        JsonParser.parseReader(bufferedReader).getAsJsonArray().forEach((entry) -> {
            JsonObject jsonObject = entry.getAsJsonObject();

            try {
                Punishment punishment = Punishment.fromJson(jsonObject);
                this.stats.compute(punishment.getType(), (_, v) -> v == null ? 1 : v + 1);
                this.addFilePunishment(this.ensureUniqueId(punishment));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to parse!", ex);
            }
        });
        this.STATE = ProviderState.READY;
        this.updateIndexes();
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            jsonWriter.beginArray().endArray();
        }
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        if (!this.canFileAction()) throw new IllegalArgumentException("Punishment provider cannot write!");
        this.STATE = ProviderState.WRITE;
        JsonArray array = new JsonArray();
        this.punishments.forEach((_, value) ->
                array.add(value.toJsonObject()));
        bufferedWriter.write(array.toString());
        this.STATE = ProviderState.READY;
    }
}
