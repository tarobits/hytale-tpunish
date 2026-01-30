package dev.tarobits.punishments.utils.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.punishment.PunishmentSubtype;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nonnull;

public class PresetConfig {
    public static final String NAME_KEY = "name";
    public static final String DURATION_KEY = "duration";
    public static final String TYPE_KEY = "type";
    public static final String SUB_TYPE_KEY = "sub_type";
    public static final String REASON_KEY = "reason";

    private final String name;
    private final TimeFormat duration;
    private final PunishmentType type;
    private final PunishmentSubtype subType;
    private final String reason;

    public PresetConfig(
            @Nonnull String name,
            @Nonnull PunishmentType type,
            @Nonnull PunishmentSubtype subType,
            @Nonnull TimeFormat duration,
            @Nonnull String reason
    ) {
        this.name = name;
        this.type = type;
        this.duration = duration;
        this.reason = reason;
        if (subType == PunishmentSubtype.NULL && (this.type == PunishmentType.BAN || this.type == PunishmentType.MUTE)) {
            throw new IllegalArgumentException("tarobits.punishments.edit.error.nonull");
        } else if (subType != PunishmentSubtype.NULL && (this.type == PunishmentType.KICK || this.type == PunishmentType.WARN)) {
            this.subType = PunishmentSubtype.NULL;
        } else {
            this.subType = subType;
        }
    }

    public String getName() { return this.name; }
    public PunishmentType getType() { return this.type; }
    public PunishmentSubtype getSubType() { return this.subType; }
    public TimeFormat getDuration() { return this.duration; }
    public String getReason() { return this.reason; }

    public static PresetConfig fromJson(JsonObject obj) throws JsonParseException {
        String name = obj.get(NAME_KEY).getAsString();
        PunishmentType type = PunishmentType.fromJson(obj.get(TYPE_KEY).getAsString());
        PunishmentSubtype subType = PunishmentSubtype.fromJson(obj.get(SUB_TYPE_KEY).getAsString());
        TimeFormat duration = TimeFormat.fromDurationString(obj.get(DURATION_KEY).getAsString());
        String reason = obj.get(REASON_KEY).getAsString();
        return new PresetConfig(name, type, subType, duration, reason);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(NAME_KEY, this.name);
        obj.addProperty(TYPE_KEY, this.type.toJson());
        obj.addProperty(SUB_TYPE_KEY, this.subType.toJson());
        obj.addProperty(DURATION_KEY, this.duration.toFullDurationString());
        obj.addProperty(REASON_KEY, this.reason);
        return obj;
    }
}
