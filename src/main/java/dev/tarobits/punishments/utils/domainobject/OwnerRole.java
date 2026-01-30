package dev.tarobits.punishments.utils.domainobject;

public enum OwnerRole {
    TARGET,
    ACTOR;

    public static OwnerRole fromJson(String id) {
        return switch (id) {
            case "target" -> TARGET;
            case "actor" -> ACTOR;
            default -> throw new IllegalArgumentException("OwnerRole ID " + id + " does not exist!");
        };
    }

    public String toJson() {
        return switch (this) {
            case TARGET -> "target";
            case ACTOR -> "actor";
        };
    }
}
