package dev.tarobits.punishments.config;

import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.model.punishment.PunishmentSubtype;
import dev.tarobits.punishments.model.punishment.PunishmentType;
import dev.tarobits.punishments.utils.TimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum ConfigSchema {
	DO_UPDATE_CHECKS("doUpdateChecks", "Enable automatic update checks", true, ConfigEntryType.BOOLEAN, 2),
	UPDATE_CHECK_FREQUENCY(
			"updateCheckFrequency",
	                       "How often update checks are performed (in hours)\nSet to 0 to disable", 2,
	                       ConfigEntryType.INTEGER, 2
	),
	DO_LOGGING("doLogging", "Enable logging of actions", true, ConfigEntryType.BOOLEAN, 2),
	DO_METRICS(
			"doMetrics",
	           "Enable anonymized metric collection while fetching updates (pluginVersion, releaseChannel)", true,
	           ConfigEntryType.BOOLEAN, 1
	),
	DEVELOPMENT_RELEASE(
			"developmentRelease", "Change update notifications to development channel", false,
	                    ConfigEntryType.BOOLEAN, 1
	),
	PRESETS(
			"presets", "Presets for the punishments", new Object() {
		List<PresetConfig> evaluate() {
			try {
				return List.of(
						new PresetConfig(
								"Hacking", PunishmentType.BAN, PunishmentSubtype.TEMPORARY,
								TimeFormat.fromDurationString("30d"), "Hacking"
						), new PresetConfig(
								"Hacking 2nd Offense", PunishmentType.BAN, PunishmentSubtype.TEMPORARY,
								TimeFormat.fromDurationString("3m"), "Hacking"
						), new PresetConfig(
								"Hate speech", PunishmentType.MUTE, PunishmentSubtype.TEMPORARY,
								TimeFormat.fromDurationString("1d"), "Hate speech"
						)
				);
			} catch (UserException e) {
				throw new DeveloperErrorException(e.getTextMessage());
			}
		}
	}.evaluate(), ConfigEntryType.PRESETS, 1
	),
	;

	private final String key;
	private final ConfigEntry entry;

	ConfigSchema(
			String key,
			String description,
			Object defaultValue,
			ConfigEntryType type,
			Integer versionAdded
	) {
		this.key = key;
		this.entry = new ConfigEntry(key, description, defaultValue, type, versionAdded);
	}

	public static Boolean doesEntryExist(String key) {
		for (ConfigSchema s : values()) {
			if (s.key.equals(key)) {
				return true;
			}
		}
		return false;
	}

	public static ConfigEntry getEntry(String key) {
		for (ConfigSchema s : values()) {
			if (s.key.equals(key)) {
				return s.entry;
			}
		}
		throw new DeveloperErrorException("Config entry with key " + key + " does not exist!");
	}

	public static Collection<ConfigEntry> getAllEntries() {
		Collection<ConfigEntry> collection = new ArrayList<>();
		for (ConfigSchema s : values()) {
			collection.add(s.entry);
		}
		return collection;
	}

	public static ConfigSchema getByKey(String key) {
		for (ConfigSchema s : values()) {
			if (s.key.equals(key)) {
				return s;
			}
		}
		throw new DeveloperErrorException("Config entry with key " + key + " does not exist!");
	}

	public String getKey() {
		return this.key;
	}

	public ConfigEntry getEntry() {
		return this.entry;
	}
}
