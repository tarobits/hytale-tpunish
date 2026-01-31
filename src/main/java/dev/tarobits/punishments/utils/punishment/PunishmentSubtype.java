package dev.tarobits.punishments.utils.punishment;

import dev.tarobits.punishments.exceptions.UserException;

public enum PunishmentSubtype {
    PERMANENT,
    TEMPORARY,
    NULL;

    public static PunishmentSubtype fromJson(String id) throws UserException {
        return switch (id) {
            case "perm", "permanent" -> PERMANENT;
            case "temp", "temporary" -> TEMPORARY;
            case "null" -> NULL;
            default -> throw new UserException("PunishmentSubtype ID " + id + " does not exist!");
        };
    }

    public String toJson() {
        return switch (this) {
            case PERMANENT -> "perm";
            case TEMPORARY -> "temp";
            case NULL -> "null";
        };
    }

    public String toDisplayString() {
        return switch (this) {
            case TEMPORARY -> "Temporary";
            case PERMANENT -> "Permanent";
            case NULL -> "Error!";
        };
    }
}
