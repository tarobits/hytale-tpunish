package dev.tarobits.punishments.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import dev.tarobits.punishments.domain.DomainObject;
import dev.tarobits.punishments.domain.DomainObjectType;
import dev.tarobits.punishments.domain.Owner;
import dev.tarobits.punishments.domain.OwnerRole;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.model.log.ExtraInfoType;
import dev.tarobits.punishments.model.log.LogActionEnum;
import dev.tarobits.punishments.model.log.LogEntry;
import dev.tarobits.punishments.model.log.LogUtils;
import dev.tarobits.punishments.provider.ConfigIdProvider;
import dev.tarobits.punishments.ui.HeaderBuilder;
import dev.tarobits.punishments.ui.UIText;
import dev.tarobits.punishments.utils.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigEntry implements DomainObject<ConfigEntry> {
	private final UUID id;
	private final String key;
	private final String description;
	private final Object defaultValue;
	private final ConfigEntryType type;
	private final Integer versionAdded;
	private Object value;

	public ConfigEntry(
			@Nonnull String key,
			@Nonnull String description,
			@Nonnull Object defaultValue,
			@Nonnull ConfigEntryType type,
			@Nonnull Integer versionAdded
	) {
		this.id = ConfigIdProvider.get()
				.getUUIDForKey(key);
		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.type = type;
		this.versionAdded = versionAdded;
	}


	public ConfigEntry(
			@Nonnull String key,
			@Nonnull String description,
			@Nonnull Object defaultValue,
			@Nonnull Object value,
			@Nonnull ConfigEntryType type,
			@Nonnull Integer versionAdded
	) {
		this.id = ConfigIdProvider.get()
				.getUUIDForKey(key);
		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = value;
		this.type = type;
		this.versionAdded = versionAdded;
	}

	public static ConfigEntry fromJson(JsonObject obj) {
		throw new DeveloperErrorException("Serialization occurs in ConfigProvider!");
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Boolean getAsBoolean() {
		if (this.type != ConfigEntryType.BOOLEAN) {
			throw new DeveloperErrorException("Cannot convert " + this.type.name() + " to Boolean!");
		}
		return (Boolean) this.value;
	}

	public Integer getAsInteger() {
		if (this.type != ConfigEntryType.INTEGER) {
			throw new DeveloperErrorException("Cannot convert " + this.type.name() + " to Integer!");
		}
		return (Integer) this.value;
	}

	public BigDecimal getAsBigDecimal() {
		if (this.type != ConfigEntryType.DECIMAL) {
			throw new DeveloperErrorException("Cannot convert " + this.type.name() + " to BigDecimal!");
		}
		return (BigDecimal) this.value;
	}

	@SuppressWarnings("unchecked")
	public List<PresetConfig> getAsPresetConfigs() {
		if (this.type != ConfigEntryType.PRESETS) {
			throw new DeveloperErrorException("Cannot convert " + this.type.name() + " to PresetConfigs!");
		}
		return (List<PresetConfig>) this.value;
	}

	public String displayValue() {
		if (this.type == ConfigEntryType.PRESETS) {
			throw new DeveloperErrorException("Cannot display PresetConfigs!");
		}
		return this.value.toString();
	}

	@Deprecated
	public Object getValue() {
		return value;
	}

	public void parseValueFromString(String value) throws InvalidActionException {
		setValue(switch (this.type) {
			case BOOLEAN -> Boolean.parseBoolean(value);
			case INTEGER -> Integer.parseInt(value);
			case DECIMAL -> new BigDecimal(value);
			default -> throw new InvalidActionException("This value cannot be set!");
		});
	}

	public void setValue(Object value) throws InvalidActionException {
		if (!validate(value)) {
			throw new InvalidActionException("Value " + value.toString() + " for setting " + this.key + " is invalid!");
		}
		this.value = value;
	}

	public ConfigEntryType getType() {
		return type;
	}

	public Integer getVersionAdded() {
		return versionAdded;
	}

	public Boolean validate(Object newValue) {
		return switch (this.type) {
			case BOOLEAN -> newValue instanceof Boolean _;
			case DECIMAL -> newValue instanceof BigDecimal i && BigDecimal.ZERO.compareTo(i) > 0;
			case INTEGER -> newValue instanceof Integer i && i >= 0;
			case PRESETS -> newValue instanceof List<?> list && list.stream()
					.allMatch(PresetConfig.class::isInstance);
		};
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public ConfigEntry withId(UUID id) throws InvalidActionException {
		throw new DeveloperErrorException("Config id cannot be changed!");
	}

	@Override
	public DomainObjectType getDomainObjectType() {
		return DomainObjectType.CONFIG_ENTRY;
	}

	@Override
	public JsonObject toJsonObject() {
		throw new DeveloperErrorException("Serialization occurs in ConfigProvider!");
	}

	@Override
	public Map<OwnerRole, Owner> getOwners() {
		throw new DeveloperErrorException("Config entries cannot have owners!");
	}

	@Override
	public Message getLogActionText(
			String logAction,
			LogEntry logEntry
	) {
		return LogActions.valueOf(logAction)
				.getLogActionText(this, logEntry);
	}

	@Override
	public UIText getLogActionUIText(String logAction) {
		return LogActions.valueOf(logAction)
				.getUIText();
	}

	@Override
	public List<HeaderBuilder.HeaderGroup> getHeader() {
		throw new DeveloperErrorException("Not implemented yet!");
	}

	public ConfigSchema getConfigSchema() {
		return ConfigSchema.getByKey(this.key);
	}

	public void parseValueFromJson(JsonElement el) throws UserException {
		this.setValue(switch (type) {
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
		});
	}

	public void parseValueToJson(JsonObject obj) {
		switch (type) {
			case BOOLEAN -> obj.addProperty(this.key, this.getAsBoolean());
			case INTEGER -> obj.addProperty(this.key, this.getAsInteger());
			case DECIMAL -> obj.addProperty(this.key, this.getAsBigDecimal());
			case PRESETS -> {
				JsonArray presetList = new JsonArray();
				for (PresetConfig presetConfig : this.getAsPresetConfigs()) {
					presetList.add(presetConfig.toJson());
				}
				obj.add(this.key, presetList);
			}
		}
	}

	public enum LogActions implements LogActionEnum<ConfigEntry> {
		MODIFY;

		@Nullable
		private final String displayColor;

		LogActions(@Nullable String displayColor) {
			this.displayColor = displayColor;
		}

		LogActions() {
			this.displayColor = null;
		}
		@Override
		public Message getLogActionText(
				ConfigEntry item,
				LogEntry logEntry
		) {
			Map<ExtraInfoType, String> extraInfo = logEntry.getExtraInfo();
			return LogUtils.prepareMessage(
					"tarobits.punishments.config.log.actions.modify", "", extraInfo.get(ExtraInfoType.PREVIOUS_VALUE),
					extraInfo.get(ExtraInfoType.NEW_VALUE), extraInfo.get(ExtraInfoType.DIFFERENCE)
			);
		}

		@Override
		public UIText getUIText() {
			return new UIText(
					StringUtils.toTitleCase(this.name()),
					Value.ref("TPunish_Styles/LogActions/ConfigEntry.ui", StringUtils.toTitleCase(this.name()))
			);
		}
	}
}
