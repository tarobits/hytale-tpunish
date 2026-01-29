package dev.tarobits.punishments;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.punishment.PunishmentSubtype;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nonnull;
import java.util.List;

public class TPunishConfig {
    private List<PresetConfig> presetConfigs = List.of(
            new PresetConfig("Hacking", PunishmentType.BAN, PunishmentSubtype.TEMPORARY, TimeFormat.fromDurationString("30d"), "Hacking"),
            new PresetConfig("Hacking 2nd Offense", PunishmentType.BAN, PunishmentSubtype.TEMPORARY, TimeFormat.fromDurationString("3m"), "Hacking"),
            new PresetConfig("Hate speech", PunishmentType.MUTE, PunishmentSubtype.TEMPORARY, TimeFormat.fromDurationString("1d"), "Hate speech")
    );

    private Boolean showUpdateNotifications = true;
    private Boolean doPunishmentLogs = true;

    public List<PresetConfig> getPresetConfigs() {
        return this.presetConfigs;
    }

    public void setPresetConfigs(List<PresetConfig> presetConfigs) {
        this.presetConfigs = presetConfigs;
    }

    public Boolean getShowUpdateNotifications() { return this.showUpdateNotifications; }

    public void setShowUpdateNotifications(Boolean newValue) { this.showUpdateNotifications = newValue; }

    public Boolean getDoPunishmentLogs() { return this.doPunishmentLogs; }

    public void setDoPunishmentLogs(Boolean newValue) { this.doPunishmentLogs = newValue; }

    public static class PresetConfig {
        private String name;
        private TimeFormat duration;
        private PunishmentType type;
        private PunishmentSubtype subtype;
        private String reason;

        public PresetConfig() {}

        public PresetConfig(
                @Nonnull String name,
                @Nonnull PunishmentType type,
                @Nonnull PunishmentSubtype subtype,
                @Nonnull TimeFormat duration,
                @Nonnull String reason
        ) {
            this.name = name;
            this.type = type;
            this.subtype = subtype;
            this.duration = duration;
            this.reason = reason;
            if (this.subtype == PunishmentSubtype.NULL && (this.type == PunishmentType.BAN || this.type == PunishmentType.MUTE)) {
                throw new IllegalArgumentException("tarobits.punishments.edit.error.nonull");
            } else if (this.subtype != PunishmentSubtype.NULL && (this.type == PunishmentType.KICK || this.type == PunishmentType.WARN)) {
                this.subtype = PunishmentSubtype.NULL;
            }
        }

        public String getName() { return this.name; }
        public PunishmentType getType() { return this.type; }
        public PunishmentSubtype getSubtype() { return this.subtype; }
        public TimeFormat getDuration() { return this.duration; }
        public String getReason() { return this.reason; }

        public static final BuilderCodec<PresetConfig> CODEC =
                BuilderCodec.builder(PresetConfig.class, PresetConfig::new)
                        .append(new KeyedCodec<>("Name", BuilderCodec.STRING), (d, v) -> d.name = v, (d) -> d.name).add()
                        .append(new KeyedCodec<>("Duration", BuilderCodec.STRING), (d, v) -> d.duration = TimeFormat.fromDurationString(v), (d) -> d.duration.toFullDurationString()).add()
                        .append(new KeyedCodec<>("Type", BuilderCodec.STRING), (d,v) -> d.type = PunishmentType.fromJson(v), (d) -> d.type.toJson()).add()
                        .append(new KeyedCodec<>("SubType", BuilderCodec.STRING), (d,v) -> d.subtype = PunishmentSubtype.fromJson(v), (d) -> d.subtype.toJson()).add()
                        .append(new KeyedCodec<>("Reason", BuilderCodec.STRING), (d,v) -> d.reason = v, (d) -> d.reason).add()
                        .build();
    }

    public static final ArrayCodec<PresetConfig> PRESET_CONFIG_LIST_CODEC =
            new ArrayCodec<>(
                    PresetConfig.CODEC,
                    PresetConfig[]::new
            );

    public static final BuilderCodec<TPunishConfig> CODEC = BuilderCodec.builder(TPunishConfig.class, TPunishConfig::new).codecVersion(3)
            .append(new KeyedCodec<>("ShowUpdateNotifications", Codec.BOOLEAN), (d,v) -> d.showUpdateNotifications = v, (d) -> d.showUpdateNotifications).add()
            .append(new KeyedCodec<>("DoPunishmentLogs", Codec.BOOLEAN), (d,v) -> d.doPunishmentLogs = v, (d) -> d.doPunishmentLogs).add()
            .append(new KeyedCodec<>("Presets", PRESET_CONFIG_LIST_CODEC), (d, v) -> d.presetConfigs = List.of(v), (d) -> d.presetConfigs.toArray(new PresetConfig[0])).add()
            .build();
}
