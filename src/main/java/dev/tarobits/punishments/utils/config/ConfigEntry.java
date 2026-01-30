package dev.tarobits.punishments.utils.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigEntry implements DomainObject<ConfigEntry> {
    private final UUID id;
    private final String key;
    private final Object defaultValue;
    private Object value;
    private final ConfigEntryType type;
    private final Integer versionAdded;


    public ConfigEntry(@Nonnull String key, @Nonnull Object defaultValue, @Nonnull ConfigEntryType type, @Nonnull Integer versionAdded) {
        this.id = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.type = type;
        this.versionAdded = versionAdded;
    }

    public ConfigEntry(@Nonnull String key, @Nonnull Object defaultValue, @Nonnull Object value, @Nonnull ConfigEntryType type, @Nonnull Integer versionAdded) {
        this.id = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
        this.type = type;
        this.versionAdded = versionAdded;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (!validate(value)) {
            throw new IllegalArgumentException("Value " + value.toString() + " for setting " + this.key +  " is invalid!");
        }
        this.value = value;
    }

    public ConfigEntryType getType() {
        return type;
    }

    public Integer getVersionAdded() {
        return versionAdded;
    }

    public Boolean validate(Object newValue) throws IllegalArgumentException {
        return switch (this.type) {
            case BOOLEAN -> newValue instanceof Boolean _;
            case DECIMAL -> newValue instanceof BigDecimal _;
            case INTEGER -> newValue instanceof Integer _;
            case PRESETS -> newValue instanceof List<?> list && list.stream().allMatch(PresetConfig.class::isInstance);
        };
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void parseValueFromJson(JsonElement el) throws JsonParseException {
        this.value = switch (type) {
            case BOOLEAN -> el.getAsBoolean();
            case INTEGER -> el.getAsInt();
            case DECIMAL -> el.getAsBigDecimal();
            case PRESETS -> {
                List<PresetConfig> presetList = new ArrayList<>();
                for (JsonElement jsonElement : el.getAsJsonArray()) {
                    presetList.add(PresetConfig.fromJson(jsonElement.getAsJsonObject()));
                }
                yield presetList;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public JsonObject parseValueToJson(JsonObject obj) {
        switch (type) {
            case BOOLEAN -> obj.addProperty(this.key, (Boolean) this.value);
            case INTEGER -> obj.addProperty(this.key, (Integer) this.value);
            case DECIMAL -> obj.addProperty(this.key, (BigDecimal) this.value);
            case PRESETS -> {
                JsonArray presetList = new JsonArray();
                for (PresetConfig presetConfig : (List<PresetConfig>) this.value) {
                    presetList.add(presetConfig.toJson());
                }
                obj.add(this.key, presetList);
            }
        }
        return obj;
    }

    @Override
    public ConfigEntry withId(UUID id) {
        throw new IllegalArgumentException("Config id cannot be changed!");
    }

    @Override
    public DomainObjectType getDomainObjectType() {
        return DomainObjectType.CONFIG_ENTRY;
    }

    @Override
    public JsonObject toJsonObject() {
        throw new IllegalArgumentException("Serialization occurs in ConfigProvider!");
    }

    @Override
    public Map<OwnerRole, Owner> getOwners() {
        throw new IllegalArgumentException("Config entries cannot have owners!");
    }

    @Override
    public void display(Player player, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {

    }

    public static ConfigEntry fromJson(JsonObject obj) {
        throw new IllegalArgumentException("Serialization occurs in ConfigProvider!");
    }
}
