package dev.tarobits.punishments.model.player;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.domain.DomainObject;
import dev.tarobits.punishments.domain.DomainObjectType;
import dev.tarobits.punishments.domain.Owner;
import dev.tarobits.punishments.domain.OwnerRole;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.model.log.LogEntry;
import dev.tarobits.punishments.ui.HeaderBuilder;
import dev.tarobits.punishments.ui.UIText;
import dev.tarobits.punishments.utils.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDomainObject implements DomainObject<PlayerDomainObject> {
	private final UUID id;
	private final String name;

	public PlayerDomainObject(
			UUID id,
			String name
	) {
		this.id = id;
		this.name = name;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public PlayerDomainObject withId(UUID id) throws InvalidActionException {
		throw new DeveloperErrorException("Player ids cannot be changed!");
	}

	@Override
	public DomainObjectType getDomainObjectType() {
		return DomainObjectType.PLAYER;
	}

	@Override
	public JsonObject toJsonObject() {
		throw new DeveloperErrorException("Players cannot be stored!");
	}

	@Override
	public Map<OwnerRole, Owner> getOwners() {
		throw new DeveloperErrorException("Players cannot have owners");
	}

	@Override
	public Message getLogActionText(
			String logAction,
			LogEntry logEntry
	) {
		throw new DeveloperErrorException("Players cannot have log actions");
	}

	@Override
	public UIText getLogActionUIText(String logAction) {
		throw new DeveloperErrorException("Players cannot have log actions");
	}

	@Override
	public List<HeaderBuilder.HeaderGroup> getHeader() {
		return List.of(new HeaderBuilder.HeaderGroup(
				"Information", List.of(
				new HeaderBuilder.HeaderElement("Username", this.name),
				new HeaderBuilder.HeaderElement("UUID", this.id.toString())
		)
		));
	}


	public List<Permissions> getPermissions() {
		List<Permissions> permissionList = new ArrayList<>();
		for (Permissions permission : Permissions.values()) {
			if (Permissions.playerHas(this.id, permission)) {
				permissionList.add(permission);
			}
		}
		return permissionList;
	}
}
