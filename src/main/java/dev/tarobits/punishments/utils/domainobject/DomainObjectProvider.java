package dev.tarobits.punishments.utils.domainobject;

import java.util.UUID;

public interface DomainObjectProvider<T extends DomainObject<T>> {
	T getFromId(UUID id);
}
