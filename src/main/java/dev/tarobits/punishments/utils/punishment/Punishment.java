package dev.tarobits.punishments.utils.punishment;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.exceptions.NoPermissionException;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.*;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;
import dev.tarobits.punishments.utils.log.ExtraInfoType;
import dev.tarobits.punishments.utils.log.LogActionEnum;
import dev.tarobits.punishments.utils.log.LogEntry;
import dev.tarobits.punishments.utils.log.LogUtils;
import dev.tarobits.punishments.utils.ui.HeaderBuilder;
import dev.tarobits.punishments.utils.ui.UIText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Punishment implements DomainObject<Punishment> {
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
	private PunishmentStatus status;
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
	) throws InvalidActionException {
		this.target = target;
		this.by = by;
		this.timestamp = timestamp;
		this.type = type;
		this.subtype = subtype;
		this.reason = reason;
		this.expiresOn = expiresOn;
		this.pardonTimestamp = null;
		this.duration = duration;
		this.id = id;
		this.status = PunishmentStatus.ACTIVE;
		this.createLogEntry(PunishmentAction.CREATE, this.by);
	}

	protected Punishment(
			@Nonnull UUID target,
			@Nonnull UUID by,
			@Nonnull Instant timestamp,
			@Nonnull PunishmentType type,
			@Nonnull PunishmentSubtype subtype,
			@Nonnull String reason,
			@Nullable Instant expiresOn,
			@Nullable Instant pardonTimestamp,
			@Nonnull TimeFormat duration,
			@Nonnull UUID id,
			@Nonnull PunishmentStatus status
	) {
		this.target = target;
		this.by = by;
		this.timestamp = timestamp;
		this.type = type;
		this.subtype = subtype;
		this.reason = reason;
		this.expiresOn = expiresOn;
		this.pardonTimestamp = pardonTimestamp;
		this.duration = duration;
		this.id = id;
		this.status = status;
	}

	public static Punishment createBan(
			UUID target,
			UUID by,
			String reason,
			TimeFormat duration
	) throws InvalidActionException, NoPermissionException {
		if (duration.isZero()) {
			return createPermBan(target, by, reason);
		}
		return createTempBan(target, by, reason, duration);
	}

	// Factories

	public static Punishment createTempBan(
			UUID target,
			UUID by,
			String reason,
			TimeFormat duration
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.BAN_TEMP)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		Instant expiresOn = duration.toInstantFromNow(currentTime);
		return new Punishment(
				target, by, currentTime, PunishmentType.BAN, PunishmentSubtype.TEMPORARY, reason,
		                      expiresOn, duration, UUID.randomUUID()
		);
	}

	public static Punishment createPermBan(
			UUID target,
			UUID by,
			String reason
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.BAN_PERM)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		return new Punishment(
				target, by, currentTime, PunishmentType.BAN, PunishmentSubtype.PERMANENT, reason, null,
		                      new TimeFormat(), UUID.randomUUID()
		);
	}

	public static Punishment createMute(
			UUID target,
			UUID by,
			String reason,
			TimeFormat duration
	) throws InvalidActionException, NoPermissionException {
		if (duration.isZero()) {
			return createPermMute(target, by, reason);
		}
		return createTempMute(target, by, reason, duration);
	}

	public static Punishment createTempMute(
			UUID target,
			UUID by,
			String reason,
			TimeFormat duration
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.MUTE_TEMP)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		Instant expiresOn = duration.toInstantFromNow(currentTime);
		return new Punishment(
				target, by, currentTime, PunishmentType.MUTE, PunishmentSubtype.TEMPORARY, reason,
		                      expiresOn, duration, UUID.randomUUID()
		);
	}

	public static Punishment createPermMute(
			UUID target,
			UUID by,
			String reason
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.MUTE_PERM)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		return new Punishment(
				target, by, currentTime, PunishmentType.MUTE, PunishmentSubtype.PERMANENT, reason, null,
		                      new TimeFormat(), UUID.randomUUID()
		);
	}

	public static Punishment createWarn(
			UUID target,
			UUID by,
			String reason
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.WARN)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		return new Punishment(
				target, by, currentTime, PunishmentType.WARN, PunishmentSubtype.NULL, reason, null,
		                      new TimeFormat(), UUID.randomUUID()
		);
	}

	public static Punishment createKick(
			UUID target,
			UUID by,
			String reason
	) throws NoPermissionException, InvalidActionException {
		if (!Punishment.canPlayerCreate(by, Permissions.KICK)) {
			throw new NoPermissionException();
		}
		Instant currentTime = Instant.now();
		return new Punishment(
				target, by, currentTime, PunishmentType.KICK, PunishmentSubtype.NULL, reason, null,
		                      new TimeFormat(), UUID.randomUUID()
		);
	}

	public static Boolean canPlayerCreate(
			@Nonnull UUID uuid,
			@Nonnull Permissions permission
	) {
		return PermissionsModule.get()
				.hasPermission(uuid, permission.getPermission());
	}

	public static Punishment fromJson(JsonObject object) {
		try {
			UUID target = UUID.fromString(object.get("target")
					                              .getAsString());
			UUID by = UUID.fromString(object.get("by")
					                          .getAsString());
			UUID id;
			try {
				id = UUID.fromString(object.get("id")
						                     .getAsString());
			} catch (IllegalArgumentException _) {
				// Migration from old ids
				id = UUID.randomUUID();
			}
			Instant timestamp = Instant.ofEpochMilli(object.get("timestamp")
					                                         .getAsLong());
			PunishmentType type = PunishmentType.fromJson(object.get("type")
					                                              .getAsString());
			PunishmentSubtype subtype = PunishmentSubtype.fromJson(object.get("subtype")
					                                                       .getAsString());
			String reason = object.get("reason")
					.getAsString();
			Instant expiresOn = null;
			if (object.has("expiresOn")) {
				expiresOn = Instant.ofEpochMilli(object.get("expiresOn")
						                                 .getAsLong());
			}
			Instant pardonedOn = null;
			if (object.has("pardonedOn")) {
				pardonedOn = Instant.ofEpochMilli(object.get("pardonedOn")
						                                  .getAsLong());
			}
			TimeFormat duration = TimeFormat.fromDurationString(object.get("duration")
					                                                    .getAsString());

			PunishmentStatus status;
			try {
				status = PunishmentStatus.valueOf(object.get("status")
						                                  .getAsString());
			} catch (Exception _) {
				// Migrate from old status
				try {
					boolean active = object.get("active")
							.getAsBoolean();
					boolean pardoned = object.get("pardoned")
							.getAsBoolean();
					if (active) {
						status = PunishmentStatus.ACTIVE;
					} else if (pardoned) {
						status = PunishmentStatus.PARDONED;
					} else {
						status = PunishmentStatus.EXPIRED;
					}
				} catch (Exception _) {
					TPunish.getLogger("Punishment")
							.atSevere()
							.log("Failed to migrate Punishment status. Defaulting to expired!");
					status = PunishmentStatus.EXPIRED;
				}
			}

			return new Punishment(
					target, by, timestamp, type, subtype, reason, expiresOn, pardonedOn, duration, id, status
			);
		} catch (Exception _) {
			throw new DeveloperErrorException("An error occurred while parsing punishments.");
		}
	}

	private PunishmentProvider getProvider() {
		return PunishmentProvider.get();
	}

	public Message pardon(UUID actor) throws InvalidActionException {
		if (this.isPardoned()) {
			throw new InvalidActionException("tarobits.punishments.edit.error.alreadypardon");
		}
		if (!this.canPardon()) {
			throw new InvalidActionException("tarobits.punishments.edit.error.notpardonable");
		}
		this.status = PunishmentStatus.PARDONED;
		this.pardonTimestamp = Instant.now();
		this.createLogEntry(PunishmentAction.PARDON, actor);
		PlayerRef target = Universe.get()
				.getPlayer(this.target);
		if (target != null) {
			target.sendMessage(
					Message.translation("tarobits.punishments." + getTranslationKey() + ".un" + getTranslationKey()));
		}
		return getProvider().updatePunishment(this)
				.param("action", Message.translation("tarobits.punishments.edit.actions.un" + this.getTranslationKey()))
				.param("player", PlayerUtils.getUsername(this.target));
	}

	public Permissions getPermission() {
		Permissions permission = Permissions.getByTranslationKey(this.getTranslationKey(), this.getSubTranslationKey());
		if (permission == null) {
			throw new DeveloperErrorException("No permission found!");
		}
		return permission;
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

	public Punishment withId(UUID id) throws InvalidActionException {
		return new Punishment(
				this.target, this.by, this.timestamp, this.type, this.subtype, this.reason,
		                      this.expiresOn, this.duration, id
		);
	}

	@Override
	public DomainObjectType getDomainObjectType() {
		return DomainObjectType.PUNISHMENT;
	}

	public JsonObject toJsonObject() {
		JsonObject object = new JsonObject();
		object.addProperty(
				"target", this.getTarget()
						.toString()
		);
		object.addProperty(
				"by", this.getBy()
						.toString()
		);
		object.addProperty(
				"id", this.getId()
						.toString()
		);
		object.addProperty(
				"timestamp", this.getTimestamp()
						.toEpochMilli()
		);
		object.addProperty(
				"type", this.getType()
						.toJson()
		);
		object.addProperty(
				"subtype", this.getSubtype()
						.toJson()
		);
		object.addProperty("reason", this.getReason());
		if (this.getExpiresOn() != null) {
			object.addProperty(
					"expiresOn", this.getExpiresOn()
							.toEpochMilli()
			);
		}
		if (this.getPardonTimestamp() != null) {
			object.addProperty(
					"pardonedOn", this.getPardonTimestamp()
							.toEpochMilli()
			);
		}
		object.addProperty(
				"duration", this.getDuration()
						.toFullDurationString()
		);
		object.addProperty(
				"status", this.getStatus()
						.name()
		);
		return object;
	}

	@Override
	public Map<OwnerRole, Owner> getOwners() {
		return Map.of(
				OwnerRole.TARGET, new Owner(DomainObjectType.PLAYER, this.target), OwnerRole.ACTOR,
				new Owner(DomainObjectType.PLAYER, this.by)
		);
	}

	@Override
	public Message getLogActionText(
			String logAction,
			LogEntry logEntry
	) {
		return PunishmentAction.valueOf(logAction)
				.getLogActionText(this, logEntry);
	}

	@Override
	public UIText getLogActionUIText(String logAction) {
		return PunishmentAction.valueOf(logAction)
				.getUIText();
	}

	@Nonnull
	public PunishmentSubtype getSubtype() {
		return subtype;
	}

	@Override
	public List<HeaderBuilder.HeaderGroup> getHeader() {
		return List.of(
				new HeaderBuilder.HeaderGroup(
						"Information", List.of(
						new HeaderBuilder.HeaderElement("Status:", this.getHeaderStatus()),
						new HeaderBuilder.HeaderElement("Type:", this.type.toDisplayString()),
						new HeaderBuilder.HeaderElement("Sub-Type:", this.getDisplaySubType()),
						new HeaderBuilder.HeaderElement("Duration:", this.getAbsoluteDuration()),
						new HeaderBuilder.HeaderElement("Reason:", this.getReason())
				)
				), new HeaderBuilder.HeaderGroup(
						"", List.of(
						new HeaderBuilder.HeaderElement(
								"Target: ", PlayerUtils.getUsername(this.getOwners()
										                                    .get(OwnerRole.TARGET)
										                                    .id())
						), new HeaderBuilder.HeaderElement(
								"Moderator: ", PlayerUtils.getUsername(this.getOwners()
										                                       .get(OwnerRole.ACTOR)
										                                       .id())
						), new HeaderBuilder.HeaderElement("Date:", this.getDate()),
						new HeaderBuilder.HeaderElement("Until:", this.getUntil()),
						new HeaderBuilder.HeaderElement("Pardoned On:", this.getPardonedDate())
				)
				)
		);
	}

	@Nonnull
	public PunishmentType getType() {
		return type;
	}

	@Nonnull
	public TimeFormat getDuration() {
		return duration;
	}

	public void createLogEntry(
			PunishmentAction action,
			UUID actor
	) throws InvalidActionException {
		if (action.unique && LogProvider.get()
				.checkIfTargetTypeExists(action.toString(), this.id, this.getDomainObjectType())) {
			throw new InvalidActionException(action + " can only exist once for punishment " + this.id);
		}
		Owner owner = new Owner(DomainObjectType.PUNISHMENT, this.id);
		LogEntry logEntry = LogEntry.getNew(owner, action.toString(), actor, Instant.now());
		LogProvider.get()
				.addEntry(logEntry);
	}

	@Nonnull
	public PunishmentStatus getStatus() {
		return status;
	}

	@Nonnull
	public String getPardonedDate() {
		if (this.status != PunishmentStatus.PARDONED || this.pardonTimestamp == null) {
			return "Never";
		}
		return TimeUtils.instantAsDateTime(this.pardonTimestamp);
	}

	@Nonnull
	public String getRelativeDuration() {
		return switch (this.subtype) {
			case PERMANENT -> "Forever";
			case TEMPORARY -> TimeFormat.fromNowToInstant(this.expiresOn)
					.toFullDurationString();
			case NULL -> "None";
		};
	}

	@Nonnull
	public String getRemainingDuration() {
		if (this.status == PunishmentStatus.PARDONED && this.pardonTimestamp != null && this.expiresOn != null) {
			return TimeFormat.fromInstantToInstant(this.pardonTimestamp, this.expiresOn)
					.toFullDurationString();
		} else if (this.status == PunishmentStatus.PARDONED) {
			return "Pardoned";
		} else {
			return this.getRelativeDuration();
		}
	}

	@Nonnull
	public String getAbsoluteDuration() {
		return switch (this.subtype) {
			case PERMANENT -> "Forever";
			case TEMPORARY -> duration.toFullDurationString(false);
			case NULL -> "None";
		};
	}

	@Nonnull
	public String getUntil() {
		return switch (this.subtype) {
			case PERMANENT -> "Forever";
			case TEMPORARY -> TimeUtils.instantAsDateTime(this.expiresOn);
			case NULL -> "None";
		};
	}

	@Nonnull
	public String getDate() {
		return TimeUtils.instantAsDate(this.timestamp);
	}

	@Nonnull
	public String getDisplaySubType() {
		return switch (this.subtype) {
			case PERMANENT -> "Permanent";
			case TEMPORARY -> "Temporary";
			case NULL -> "None";
		};
	}

	@Nonnull
	public Boolean isActive() {
		this.checkIfActive();
		return this.status == PunishmentStatus.ACTIVE;
	}

	public Boolean isPardoned() {
		return this.status == PunishmentStatus.PARDONED;
	}

	public Boolean canExpire() {
		return this.subtype.canExpire && this.status == PunishmentStatus.ACTIVE;
	}

	public Boolean canReduce() {
		return false;
		// ToDo: Add reducing
		//return this.subtype.canReduce && this.status == PunishmentStatus.ACTIVE;
	}

	public Boolean canExtend() {
		return false;
		// ToDo: Add extending
		//return this.subtype.canExtend && this.status == PunishmentStatus.ACTIVE;
	}

	public Boolean canPardon() {
		return this.type.canPardon && this.status == PunishmentStatus.ACTIVE;
	}

	@Nonnull
	public Message getReasonMessage() {
		return Message.translation("tarobits.punishments." + this.getTranslationKey() + ".reason")
				.param("type", this.getDisplaySubType())
				.param("duration", this.getAbsoluteDuration())
				.param("until", this.getUntil())
				.param("reason", this.getReason());
	}

	@Nonnull
	public Message getSuccessMessage(Message userName) {
		return Message.translation(
						"tarobits.punishments." + this.getTranslationKey() + ".success." + this.getSubTranslationKey())
				.param("name", userName)
				.param("duration", this.getAbsoluteDuration())
				.param("reason", this.getReason());
	}

	public Boolean canReinstate() {
		return false;
		// ToDo: Add reinstating
		//return this.status == PunishmentStatus.PARDONED;
	}

	public String getTranslationKey() {
		return switch (this.type) {
			case BAN -> "ban";
			case MUTE -> "mute";
			case WARN -> "warn";
			case KICK -> "kick";
		};
	}

	public String getSubTranslationKey() {
		return switch (this.subtype) {
			case PERMANENT -> "perm";
			case TEMPORARY -> "temp";
			case NULL -> "";
		};
	}

	protected void checkIfActive() {
		if (this.status == PunishmentStatus.ACTIVE && this.expiresOn != null && this.expiresOn.isBefore(
				Instant.now())) {
			this.status = PunishmentStatus.EXPIRED;
		}
	}

	private UIText getHeaderStatus() {
		if (this.status == PunishmentStatus.PARDONED) {
			return new UIText("Pardoned", Value.ref("Tarobits_Punishments_Styles.ui", "PardonedStyle"));
		}
		if (this.status == PunishmentStatus.ACTIVE) {
			return new UIText("Active", Value.ref("Tarobits_Punishments_Styles.ui", "ActiveStyle"));
		}
		return new UIText("Expired", Value.ref("Tarobits_Punishments_Styles.ui", "ExpiredStyle"));
	}


	public enum PunishmentAction implements LogActionEnum<Punishment> {
		CREATE(true),
		REDUCE,
		EXTEND,
		PARDON,
		EXPIRE;

		public final Boolean unique;

		PunishmentAction(Boolean unique) {
			this.unique = unique;
		}

		PunishmentAction() {
			this.unique = false;
		}

		@Override
		public Message getLogActionText(
				Punishment item,
				LogEntry logEntry
		) {
			Map<ExtraInfoType, String> extraInfo = logEntry.getExtraInfo();
			return switch (this) {
				case CREATE -> LogUtils.prepareMessage(
						"tarobits.punishments." + item.getTranslationKey() + ".log.action.create",
						PlayerUtils.getUsername(item.getOwners()
								                        .get(OwnerRole.TARGET)
								                        .id()), "", "", ""
				);
				case REDUCE -> LogUtils.prepareMessage(
						"tarobits.punishments." + item.getTranslationKey() + ".log.action.reduce",
						PlayerUtils.getUsername(item.getOwners()
								                        .get(OwnerRole.TARGET)
								                        .id()), extraInfo.get(ExtraInfoType.PREVIOUS_VALUE),
						extraInfo.get(ExtraInfoType.NEW_VALUE), extraInfo.get(ExtraInfoType.DIFFERENCE)
				);
				case EXTEND -> LogUtils.prepareMessage(
						"tarobits.punishments." + item.getTranslationKey() + ".log.action.extend",
						PlayerUtils.getUsername(item.getOwners()
								                        .get(OwnerRole.TARGET)
								                        .id()), extraInfo.get(ExtraInfoType.PREVIOUS_VALUE),
						extraInfo.get(ExtraInfoType.NEW_VALUE), extraInfo.get(ExtraInfoType.DIFFERENCE)
				);
				case PARDON -> LogUtils.prepareMessage(
						"tarobits.punishments." + item.getTranslationKey() + ".log.action.pardon",
						PlayerUtils.getUsername(item.getOwners()
								                        .get(OwnerRole.TARGET)
								                        .id()), "", "", ""
				);
				case EXPIRE -> LogUtils.prepareMessage(
						"tarobits.punishments." + item.getTranslationKey() + ".log.action.expire", "", "", "", "");
			};
		}

		@Override
		public UIText getUIText() {
			return new UIText(
					StringUtils.toTitleCase(this.name()),
			                  Value.ref("TPunish_Styles/LogActions/Punishment.ui", StringUtils.toTitleCase(this.name()))
			);
		}
	}
}
