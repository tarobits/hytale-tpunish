package dev.tarobits.punishments.utils.log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class LogEntry implements DomainObject<LogEntry> {
	private static final LogProvider PROVIDER = LogProvider.get();
	private final UUID id;
	private final Owner owner;
	private final String action;
	private final UUID actor;
	private final Instant timestamp;
	@Nullable
	private final String extraInfo;

	private LogEntry(
			@Nonnull UUID id,
			@Nonnull Owner owner,
			@Nonnull String action,
			@Nonnull UUID actor,
			@Nonnull Instant timestamp,
			@Nullable String extraInfo
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
			@Nullable String extraInfo
	) {
		return new LogEntry(UUID.randomUUID(), owner, action, actor, timestamp, extraInfo);
	}

	public static LogEntry getNew(
			@Nonnull Owner owner,
			@Nonnull String action,
			@Nonnull UUID actor,
			@Nonnull Instant timestamp
	) {
		return getNew(owner, action, actor, timestamp, null);
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
			String extraInfo = null;
			if (obj.has("extra_info")) {
				extraInfo = obj.get("extra_info")
						.getAsString();
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
		if (this.extraInfo != null) {
			obj.addProperty("extra_info", this.extraInfo);
		}
		return obj;
	}

	@Override
	public Map<OwnerRole, Owner> getOwners() {
		return Map.of(OwnerRole.TARGET, this.owner, OwnerRole.ACTOR, new Owner(DomainObjectType.PLAYER, this.actor));
	}

	@Override
	public String getLogActionText(String logAction) {
		throw new DeveloperErrorException("LogEntries cannot have log action text!");
	}

	@Override
	public void display(
			Player player,
			PlayerRef playerRef,
			Ref<EntityStore> ref,
			Store<EntityStore> store
	) {
		throw new DeveloperErrorException("Log Entry cannot be displayed!");
	}

	public String getAction() {
		return this.action;
	}

	public Instant getTimestamp() {
		return this.timestamp;
	}

	@Nullable
	public String getExtraInfo() {
		return this.extraInfo;
	}
}
