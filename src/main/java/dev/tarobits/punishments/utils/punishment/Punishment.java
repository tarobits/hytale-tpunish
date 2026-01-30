package dev.tarobits.punishments.utils.punishment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.exceptions.NoPermissionException;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.TimeUtils;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;
import dev.tarobits.punishments.utils.ui.PunishmentEntryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Punishment implements DomainObject<Punishment> {
    private static final PunishmentProvider PROVIDER = PunishmentProvider.get();
    @Nonnull
    private final UUID target;
    @Nonnull
    private final UUID by;
    @Nonnull
    private final Instant timestamp;
    @Nonnull
    private final PunishmentType type;
    @Nonnull
    private final PunishmentSubtype subtype;
    @Nonnull
    private final String reason;
    @Nullable
    private final Instant expiresOn;
    @Nonnull
    private final TimeFormat duration;
    @Nonnull
    private final UUID id;
    @Nonnull
    private Boolean active;
    @Nonnull
    private Boolean pardoned;
    @Nullable
    private Instant pardonTimestamp;

    public Punishment(
            @Nonnull UUID target,
            @Nonnull UUID by,
            @Nonnull Instant timestamp,
            @Nonnull PunishmentType type,
            @Nonnull PunishmentSubtype subtype,
            @Nonnull String reason,
            @Nullable Instant expiresOn,
            @Nonnull TimeFormat duration,
            @Nonnull UUID id
    ) {
        this.target = target;
        this.by = by;
        this.timestamp = timestamp;
        this.type = type;
        this.subtype = subtype;
        this.reason = reason;
        this.expiresOn = expiresOn;
        this.duration = duration;
        this.id = id;
        this.active = true;
        this.pardoned = false;
    }

    protected Punishment(
            @Nonnull UUID target,
            @Nonnull UUID by,
            @Nonnull Instant timestamp,
            @Nonnull PunishmentType type,
            @Nonnull PunishmentSubtype subtype,
            @Nonnull String reason,
            @Nullable Instant expiresOn,
            @Nonnull TimeFormat duration,
            @Nonnull UUID id,
            @Nonnull Boolean active,
            @Nonnull Boolean pardoned
    ) {
        this.target = target;
        this.by = by;
        this.timestamp = timestamp;
        this.type = type;
        this.subtype = subtype;
        this.reason = reason;
        this.expiresOn = expiresOn;
        this.duration = duration;
        this.id = id;
        this.active = active;
        this.pardoned = pardoned;
    }

    // Factories

    public static Punishment createBan(UUID target, UUID by, String reason, TimeFormat duration) {
        if (duration.isZero()) {
            return createPermBan(target, by, reason);
        }
        return createTempBan(target, by, reason, duration);
    }

    public static Punishment createTempBan(UUID target, UUID by, String reason, TimeFormat duration) {
        if (!Punishment.canPlayerCreate(by, Permissions.BAN_TEMP)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        Instant expiresOn = duration.toInstantFromNow(currentTime);
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.BAN,
                PunishmentSubtype.TEMPORARY,
                reason,
                expiresOn,
                duration,
                UUID.randomUUID()
        );
    }
    public static Punishment createPermBan(UUID target, UUID by, String reason) {
        if (!Punishment.canPlayerCreate(by, Permissions.BAN_PERM)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.BAN,
                PunishmentSubtype.PERMANENT,
                reason,
                null,
                new TimeFormat(),
                UUID.randomUUID()
        );
    }
    public static Punishment createMute(UUID target, UUID by, String reason, TimeFormat duration) {
        if (duration.isZero()) {
            return createPermMute(target, by, reason);
        }
        return createTempMute(target, by, reason, duration);
    }
    public static Punishment createTempMute(UUID target, UUID by, String reason, TimeFormat duration) {
        if (!Punishment.canPlayerCreate(by, Permissions.MUTE_TEMP)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        Instant expiresOn = duration.toInstantFromNow(currentTime);
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.MUTE,
                PunishmentSubtype.TEMPORARY,
                reason,
                expiresOn,
                duration,
                UUID.randomUUID()
        );
    }
    public static Punishment createPermMute(UUID target, UUID by, String reason) {
        if (!Punishment.canPlayerCreate(by, Permissions.MUTE_PERM)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.MUTE,
                PunishmentSubtype.PERMANENT,
                reason,
                null,
                new TimeFormat(),
                UUID.randomUUID()
        );
    }
    public static Punishment createWarn(UUID target, UUID by, String reason) {
        if (!Punishment.canPlayerCreate(by, Permissions.WARN)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.WARN,
                PunishmentSubtype.NULL,
                reason,
                null,
                new TimeFormat(),
                UUID.randomUUID()
        );
    }
    public static Punishment createKick(UUID target, UUID by, String reason) {
        if (!Punishment.canPlayerCreate(by, Permissions.KICK)) {
            throw new NoPermissionException();
        }
        Instant currentTime = Instant.now();
        return new Punishment(
                target,
                by,
                currentTime,
                PunishmentType.KICK,
                PunishmentSubtype.NULL,
                reason,
                null,
                new TimeFormat(),
                UUID.randomUUID()
        );
    }

    public Punishment withId(UUID id) {
        return new Punishment(
                this.target,
                this.by,
                this.timestamp,
                this.type,
                this.subtype,
                this.reason,
                this.expiresOn,
                this.duration,
                id
        );
    }

    public Message pardon() {
        this.pardoned = true;
        this.active = false;
        return PROVIDER.updatePunishment(this, this.id).param("action", Message.translation("tarobits.punishments.edit.actions.un" + this.getTranslationKey())).param("player", PunishmentEntryBuilder.getActorName(this.target));

        // ToDo: Implement logging
    }

    @Override
    public void display(Player player, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        // ToDo: Add punishment display page
    }

    public Permissions getPermission() {
        Permissions permission = Permissions.getByTranslationKey(this.getTranslationKey(), this.getSubTranslationKey());
        if (permission == null) throw new IllegalArgumentException("No permission found!");
        return permission;
    }

    public static Boolean canPlayerCreate(@Nonnull UUID uuid, @Nonnull Permissions permission) {
        return PermissionsModule.get().hasPermission(uuid, permission.getPermission());
    }

    @Nonnull
    public UUID getTarget() {
        return target;
    }

    @Nonnull
    public UUID getBy() {
        return by;
    }

    @Nonnull
    public String getReason() {
        return reason;
    }

    @Nonnull
    public Instant getTimestamp() {
        return timestamp;
    }

    @Nullable
    public Instant getExpiresOn() {
        return expiresOn;
    }

    @Nullable
    public Instant getPardonTimestamp() {
        return pardonTimestamp;
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    @Nonnull
    public PunishmentSubtype getSubtype() {
        return subtype;
    }

    @Override
    public DomainObjectType getDomainObjectType() {
        return DomainObjectType.PUNISHMENT;
    }

    @Nonnull
    public PunishmentType getType() {
        return type;
    }

    @Nonnull
    public TimeFormat getDuration() {
        return duration;
    }

    @Override
    public Map<OwnerRole, Owner> getOwners() {
        return Map.of(
                OwnerRole.TARGET, new Owner(DomainObjectType.PLAYER, this.target),
                OwnerRole.ACTOR, new Owner(DomainObjectType.PLAYER, this.by)
        );
    }

    @Nonnull
    public String getRelativeDuration() {
        return switch (this.subtype) {
            case PERMANENT -> "Forever";
            case TEMPORARY -> TimeFormat.fromNowToInstant(this.expiresOn).toFullDurationString();
            case NULL -> "ERROR!";
        };
    }

    @Nonnull
    public String getAbsoluteDuration() {
        return switch (this.subtype) {
            case PERMANENT -> "Forever";
            case TEMPORARY -> duration.toFullDurationString(false);
            case NULL -> "ERROR!";
        };
    }

    @Nonnull
    public String getUntil() {
        return switch (this.subtype) {
            case PERMANENT -> "Forever";
            case TEMPORARY -> TimeUtils.instantAsDateTime(this.expiresOn);
            case NULL -> "ERROR!";
        };
    }

    @Nonnull
    public String getDisplaySubType() {
        return switch (this.subtype) {
            case PERMANENT -> "Permanent";
            case TEMPORARY -> "Temporary";
            case NULL -> "ERROR!";
        };
    }

    @Nonnull
    public String getDate() {
        return TimeUtils.instantAsDate(this.timestamp);
    }

    @Nonnull
    public Boolean isActive() {
        this.checkIfActive();
        return this.active;
    }

    public Boolean isPardoned() {
        return this.pardoned;
    }

    @Nonnull
    public Message getReasonMessage() {
        return Message.translation("tarobits.punishments." + this.getTranslationKey() + ".reason").param("type", this.getDisplaySubType()).param("duration", this.getAbsoluteDuration()).param("until", this.getUntil()).param("reason", this.getReason());
    }

    @Nonnull
    public Message getSuccessMessage(Message userName) {
        return Message.translation("tarobits.punishments." + this.getTranslationKey() + ".success." + this.getSubTranslationKey()).param("name", userName).param("duration", this.getAbsoluteDuration()).param("reason", this.getReason());
    }

    protected String getTranslationKey() {
        return switch (this.type) {
            case BAN -> "ban";
            case MUTE -> "mute";
            case WARN -> "warn";
            case KICK -> "kick";
        };
    }

    protected String getSubTranslationKey() {
        return switch (this.subtype) {
            case PERMANENT -> "perm";
            case TEMPORARY -> "temp";
            case NULL -> "";
        };
    }

    protected void checkIfActive() {
        this.active = !this.pardoned &&
                (this.expiresOn == null || this.expiresOn.isAfter(Instant.now()));
    }

    public static Punishment fromJson(JsonObject object) {
        try {
            UUID target = UUID.fromString(object.get("target").getAsString());
            UUID by = UUID.fromString(object.get("by").getAsString());
            UUID id;
            try {
                id = UUID.fromString(object.get("id").getAsString());
            } catch (IllegalArgumentException _) {
                // Migration from old ids
                id = UUID.randomUUID();
            }
            Instant timestamp = Instant.ofEpochMilli(object.get("timestamp").getAsLong());
            PunishmentType type = PunishmentType.fromJson(object.get("type").getAsString());
            PunishmentSubtype subtype = PunishmentSubtype.fromJson(object.get("subtype").getAsString());
            String reason = object.get("reason").getAsString();
            Instant expiresOn = null;
            if (object.has("expiresOn")) {
                expiresOn = Instant.ofEpochMilli(object.get("expiresOn").getAsLong());
            }
            TimeFormat duration = TimeFormat.fromDurationString(object.get("duration").getAsString());
            Boolean active = object.get("active").getAsBoolean();
            Boolean pardoned = object.get("pardoned").getAsBoolean();
            return new Punishment(target, by, timestamp, type, subtype, reason, expiresOn, duration, id, active, pardoned);
        } catch (Exception _) {
            throw new IllegalArgumentException("An error occurred while parsing punishments.");
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("target", this.getTarget().toString());
        object.addProperty("by", this.getBy().toString());
        object.addProperty("id", this.getId().toString());
        object.addProperty("timestamp", this.getTimestamp().toEpochMilli());
        object.addProperty("type", this.getType().toJson());
        object.addProperty("subtype", this.getSubtype().toJson());
        object.addProperty("reason", this.getReason());
        if (this.getExpiresOn() != null) {
            object.addProperty("expiresOn", this.getExpiresOn().toEpochMilli());
        }
        object.addProperty("duration", this.getDuration().toFullDurationString());
        object.addProperty("active", this.isActive());
        object.addProperty("pardoned", this.isPardoned());
        return object;
    }
}
