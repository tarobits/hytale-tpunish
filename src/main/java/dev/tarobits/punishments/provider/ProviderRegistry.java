package dev.tarobits.punishments.provider;

import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import io.netty.util.SuppressForbidden;

import java.util.Map;

public class ProviderRegistry {
    private static ProviderRegistry INSTANCE;
    private final Map<DomainObjectType, AbstractProvider<?>> providers = Map.of(
            DomainObjectType.PUNISHMENT, PunishmentProvider.get(),
            DomainObjectType.LOG_ENTRY, LogProvider.get(),
            DomainObjectType.CONFIG_ENTRY, ConfigProvider.get()
    );

    public ProviderRegistry() {
        INSTANCE = this;
    }

    public static ProviderRegistry get() {
        if (INSTANCE == null) {
            INSTANCE = new ProviderRegistry();
        }
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainObject<T>> AbstractProvider<T> getProvider(DomainObjectType type) {
        AbstractProvider<?> provider = providers.get(type);

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Provider for type " + type.toJson() + " does not exist!"
            );
        }

        return (AbstractProvider<T>) provider;
    }
}
