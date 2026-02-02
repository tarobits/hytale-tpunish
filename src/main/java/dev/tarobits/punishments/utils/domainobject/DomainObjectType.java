package dev.tarobits.punishments.utils.domainobject;

import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.provider.AbstractProvider;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.provider.PunishmentProvider;

public enum DomainObjectType {
	LOG_ENTRY,
	PUNISHMENT,
	CONFIG_ENTRY,
	PLAYER;

	public AbstractProvider<?> getProvider() {
		return switch (this) {
			case LOG_ENTRY -> LogProvider.get();
			case PUNISHMENT -> PunishmentProvider.get();
			case CONFIG_ENTRY -> ConfigProvider.get();
			default -> throw new DeveloperErrorException("Unknown DomainObjectType");
		};
	}
}
