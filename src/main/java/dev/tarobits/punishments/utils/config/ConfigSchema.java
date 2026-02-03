package dev.tarobits.punishments.utils.config;

import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.punishment.PunishmentSubtype;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum ConfigSchema {
	DO_UPDATE_CHECKS("doUpdateChecks", true, ConfigEntryType.BOOLEAN, 2),
	UPDATE_CHECK_FREQUENCY("updateCheckFrequency", TimeFormat.fromDurationString("2h"), ConfigEntryType.FREQUENCY, 2),
	DO_LOGGING("doLogging", true, ConfigEntryType.BOOLEAN, 2),
	DO_METRICS("doMetrics", true, ConfigEntryType.BOOLEAN, 1),
	DEVELOPMENT_RELEASE("developmentRelease", false, ConfigEntryType.BOOLEAN, 1),
	PRESETS(
			"presets", new Object() {
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
			Object defaultValue,
			ConfigEntryType type,
			Integer versionAdded
	) {
		this.key = key;
		this.entry = new ConfigEntry(key, defaultValue, type, versionAdded);
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
