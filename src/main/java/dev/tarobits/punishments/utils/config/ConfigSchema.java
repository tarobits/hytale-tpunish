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
	DO_PUNISHMENT_LOGS("doPunishmentLogs", true, ConfigEntryType.BOOLEAN, 1),
	SHOW_UPDATE_NOTIFICATIONS("showUpdateNotifications", true, ConfigEntryType.BOOLEAN, 1);

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

	public String getKey() {
		return this.key;
	}

	public ConfigEntry getEntry() {
		return this.entry;
	}
}
