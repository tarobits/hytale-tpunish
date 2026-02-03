package dev.tarobits.punishments.utils;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;
import dev.tarobits.punishments.utils.log.LogEntry;
import dev.tarobits.punishments.utils.ui.HeaderBuilder;
import dev.tarobits.punishments.utils.ui.UIText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayerDomainObject(UUID id, String name) implements DomainObject<PlayerDomainObject> {


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
