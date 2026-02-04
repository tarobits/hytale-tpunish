package dev.tarobits.punishments.domain;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.model.log.LogEntry;
import dev.tarobits.punishments.ui.HeaderBuilder;
import dev.tarobits.punishments.ui.UIText;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DomainObject<T extends DomainObject<T>> {
	UUID getId();

	T withId(UUID id) throws InvalidActionException;

	DomainObjectType getDomainObjectType();

	JsonObject toJsonObject();

	Map<OwnerRole, Owner> getOwners();

	Message getLogActionText(
			String logAction,
			LogEntry logEntry
	);

	UIText getLogActionUIText(String logAction);

	List<HeaderBuilder.HeaderGroup> getHeader();
}
