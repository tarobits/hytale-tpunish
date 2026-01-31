package dev.tarobits.punishments.utils.domainobject;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.exceptions.InvalidActionException;

import java.util.Map;
import java.util.UUID;

public interface DomainObject<T extends DomainObject<T>> {
	UUID getId();

	T withId(UUID id) throws InvalidActionException;

	DomainObjectType getDomainObjectType();

	JsonObject toJsonObject();

	Map<OwnerRole, Owner> getOwners();

	String getLogActionText(String logAction);

	void display(
			Player player,
			PlayerRef playerRef,
			Ref<EntityStore> ref,
			Store<EntityStore> store
	);
}
