package dev.tarobits.punishments.model.log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.domain.DomainObject;
import dev.tarobits.punishments.domain.DomainObjectType;
import dev.tarobits.punishments.domain.Owner;
import dev.tarobits.punishments.domain.OwnerRole;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.ui.HeaderBuilder;
import dev.tarobits.punishments.ui.UIText;
import dev.tarobits.punishments.utils.PlayerUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 *
 * Standardized placeholders:
 * target: target of the action
 * actor: actor always a player
 * prevValue: previous value (e.g. settings change)
 * newValue: new value (e.g. settings change)
 * diff: difference (e.g. punishment extension or reduction)
 * arg1: argument 1 (variable use)
 * arg2: argument 2 (variable use)
 * arg3: argument 3 (variable use)
 */

public class LogEntry implements DomainObject<LogEntry> {
	private static final LogProvider PROVIDER = LogProvider.get();
	private final UUID id;
	private final Owner owner;
	private final String action;
	private final UUID actor;
	private final Instant timestamp;
	private final Map<ExtraInfoType, String> extraInfo;

	private LogEntry(
			@Nonnull UUID id,
			@Nonnull Owner owner,
			@Nonnull String action,
			@Nonnull UUID actor,
			@Nonnull Instant timestamp,
			@Nonnull Map<ExtraInfoType, String> extraInfo
	) {
		this.id = id;
		this.owner = owner;
		this.action = action;
		this.actor = actor;
		this.timestamp = timestamp;
		this.extraInfo = extraInfo;
	}

	public static LogEntry getNew(
			@Nonnull Owner owner,
			@Nonnull String action,
			@Nonnull UUID actor,
			@Nonnull Instant timestamp,
			@Nonnull Map<ExtraInfoType, String> extraInfo
	) {
		return new LogEntry(UUID.randomUUID(), owner, action, actor, timestamp, extraInfo);
	}

	public static LogEntry getNew(
			@Nonnull Owner owner,
			@Nonnull String action,
			@Nonnull UUID actor,
			@Nonnull Instant timestamp
	) {
		return getNew(owner, action, actor, timestamp, new Object2ObjectOpenHashMap<>());
	}

	public static LogEntry fromJson(@Nonnull JsonObject obj) {
		try {
			UUID id = UUID.fromString(obj.get("id")
					                          .getAsString());
			Owner owner = Owner.fromJson(obj.get("owner")
					                             .getAsJsonObject());
			String action = obj.get("action")
					.getAsString();
			UUID actor = UUID.fromString(obj.get("actor")
					                             .getAsString());
			Instant timestamp = Instant.ofEpochMilli(obj.get("timestamp")
					                                         .getAsLong());
			Map<ExtraInfoType, String> extraInfo = new Object2ObjectOpenHashMap<>();
			if (obj.has("extra_info")) {
				JsonObject infoObj = obj.getAsJsonObject("extra_info");
				for (ExtraInfoType extraInfoType : ExtraInfoType.values()) {
					if (infoObj.has(extraInfoType.name())) {
						extraInfo.put(extraInfoType, infoObj.get(extraInfoType.name())
								.getAsString()
						);
					}
				}
			}
			return new LogEntry(id, owner, action, actor, timestamp, extraInfo);
		} catch (JsonParseException _) {
			throw new DeveloperErrorException("An error occurred while parsing logs.");
		}
	}

	public UUID getId() {
		return id;
	}

	@Override
	public LogEntry withId(UUID id) throws InvalidActionException {
		return new LogEntry(id, this.owner, this.action, this.actor, this.timestamp, this.extraInfo);
	}

	public DomainObjectType getDomainObjectType() {
		return DomainObjectType.LOG_ENTRY;
	}

	public JsonObject toJsonObject() {
		JsonObject obj = new JsonObject();
		obj.addProperty("id", this.id.toString());
		obj.add("owner", owner.toJsonObject());
		obj.addProperty("action", this.action);
		obj.addProperty("actor", this.actor.toString());
		obj.addProperty("timestamp", this.timestamp.toEpochMilli());
		JsonObject extraInfoObject = new JsonObject();
		for (ExtraInfoType extraInfoType : ExtraInfoType.values()) {
			if (extraInfo.containsKey(extraInfoType)) {
				extraInfoObject.addProperty(extraInfoType.name(), extraInfo.get(extraInfoType));
			}
		}
		obj.add("extra_info", extraInfoObject);
		return obj;
	}

	@Override
	public Map<OwnerRole, Owner> getOwners() {
		return Map.of(OwnerRole.TARGET, this.owner, OwnerRole.ACTOR, new Owner(DomainObjectType.PLAYER, this.actor));
	}

	@Override
	public Message getLogActionText(
			String logAction,
			LogEntry logEntry
	) {
		throw new DeveloperErrorException("LogEntries cannot have log action text!");
	}

	@Override
	public UIText getLogActionUIText(String logAction) {
		throw new DeveloperErrorException("LogEntries cannot have log action text!");
	}

	@Override
	public List<HeaderBuilder.HeaderGroup> getHeader() {
		throw new DeveloperErrorException("Log Entry cannot be displayed!");
	}

	public Message getTargetLogActionText() {
		Owner target = this.getOwners()
				.get(OwnerRole.TARGET);
		DomainObject<?> item = target.type()
				.getProvider()
				.getFromId(target.id());
		return item.getLogActionText(this.action, this)
				.param("actor", PlayerUtils.getUsername(this.actor));
	}

	public UIText getTargetLogActionUIText() {
		Owner target = this.getOwners()
				.get(OwnerRole.TARGET);
		DomainObject<?> item = target.type()
				.getProvider()
				.getFromId(target.id());
		return item.getLogActionUIText(this.action);
	}

	public String getAction() {
		return this.action;
	}

	public Instant getTimestamp() {
		return this.timestamp;
	}

	public Map<ExtraInfoType, String> getExtraInfo() {
		return this.extraInfo;
	}
}
