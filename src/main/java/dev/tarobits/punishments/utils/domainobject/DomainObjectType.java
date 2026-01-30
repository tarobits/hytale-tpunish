package dev.tarobits.punishments.utils.domainobject;

public enum DomainObjectType {
    LOG_ENTRY,
    PUNISHMENT,
    CONFIG_ENTRY,
    PLAYER;

    public static DomainObjectType fromJson(String id) {
        return switch (id) {
            case "log_entry" -> LOG_ENTRY;
            case "punishment" -> PUNISHMENT;
            case "config_entry","setting" -> CONFIG_ENTRY;
            case "player" -> PLAYER;
            default -> throw new IllegalArgumentException("DomainObjectType ID " + id + " does not exist!");
        };
    }

    public String toJson() {
        return switch (this) {
            case LOG_ENTRY -> "log_entry";
            case PUNISHMENT -> "punishment";
            case CONFIG_ENTRY -> "config_entry";
            case PLAYER -> "player";
        };
    }
}
