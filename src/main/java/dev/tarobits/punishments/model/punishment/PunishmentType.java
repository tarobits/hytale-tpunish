package dev.tarobits.punishments.model.punishment;

import dev.tarobits.punishments.exceptions.UserException;

public enum PunishmentType {
    BAN(true),
    MUTE(true),
    WARN(false),
    KICK(false);

    public final boolean canPardon;

    PunishmentType(boolean canPardon) {
        this.canPardon = canPardon;
    }

    public static PunishmentType fromJson(String id) throws UserException {
        return switch (id) {
            case "ban" -> BAN;
            case "mute" -> MUTE;
            case "warn" -> WARN;
            case "kick" -> KICK;
            default -> throw new UserException("PunishmentType ID " + id + " does not exist!");
        };
    }

    public String toJson() {
        return switch (this) {
            case BAN -> "ban";
            case MUTE -> "mute";
            case WARN -> "warn";
            case KICK -> "kick";
        };
    }

    public String toDisplayString() {
        return switch (this) {
            case BAN -> "Ban";
            case MUTE -> "Mute";
            case WARN -> "Warn";
            case KICK -> "Kick";
        };
    }
}

