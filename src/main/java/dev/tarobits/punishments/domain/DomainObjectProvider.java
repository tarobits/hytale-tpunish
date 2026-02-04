package dev.tarobits.punishments.domain;

import java.util.UUID;

public interface DomainObjectProvider<T extends DomainObject<T>> {
	T getFromId(UUID id);
}
