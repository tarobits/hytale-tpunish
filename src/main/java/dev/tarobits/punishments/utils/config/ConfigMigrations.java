package dev.tarobits.punishments.utils.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.provider.ConfigProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.logging.Level;

public class ConfigMigrations {
	private static final HytaleLogger LOGGER = TPunish.getLogger("ConfigMigrations");

	private static void log(String msg) {
		LOGGER.atInfo()
				.log(msg);
	}

	public static JsonObject performMigrations(
			JsonObject currentConfig,
			Integer currentVersion
	) {
		log("-=-=-= Running config migrations =-=-=-");
		JsonObject result = new JsonObject();
		while (currentVersion < ConfigProvider.getConfigVersion()) {
			migrate(currentVersion, currentConfig, result);
			currentVersion++;
		}

		normalizeConfig(currentConfig, result);

		log("-=-=-= Config migrations END =-=-=-");
		return result;
	}

	private static void migrate(
			Integer currentVersion,
			JsonObject currentConfig,
			JsonObject result
	) {
		switch (currentVersion) {
			case 0:
				zeroToOne(currentConfig, result);
				break;
			case 1:
				oneToTwo(currentConfig, result);
				break;
			// Add more migrations here
		}
	}

	// Clean config and add missing settings
	private static void normalizeConfig(
			JsonObject currentConfig,
			JsonObject result
	) {
		for (ConfigSchema s : ConfigSchema.values()) {
			if (!result.has(s.getKey())) {
				ConfigEntry entry = s.getEntry();
				try {
					entry.parseValueFromJson(currentConfig.get(s.getKey()));
				} catch (Exception _) {
					LOGGER.at(Level.FINE)
							.log("Failed to find config key " + s.getKey() + " using default.");
				}
				entry.parseValueToJson(result);
			}
		}
	}

	private static void oneToTwo(
			JsonObject currentConfig,
			JsonObject result
	) {
		log("Running migration to config version 2");

		try {
			result.addProperty("doUpdateChecks", currentConfig.get("showUpdateNotifications")
					.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doUpdateChecks", true);
		}

		try {
			result.addProperty("doLogging", currentConfig.get("doPunishmentLogs")
					.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doPunishmentLogs", true);
		}
	}

	private static void zeroToOne(
			JsonObject currentConfig,
			JsonObject result
	) {
		log("Running migration to config version 1");
		try {
			result.addProperty(
					"doPunishmentNotifications", currentConfig.get("DoPunishmentNotifications")
							.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doPunishmentNotifications", true);
		}

		try {
			result.addProperty(
					"showUpdateNotifications", currentConfig.get("ShowUpdateNotifications")
							.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("showUpdateNotifications", true);
		}

		try {
			JsonArray newPresets = new JsonArray();
			currentConfig.get("Presets")
					.getAsJsonArray()
					.forEach((e) -> {
						JsonObject obj = e.getAsJsonObject();
						JsonObject newObj = new JsonObject();

						newObj.addProperty(
								"name", obj.get("Name")
										.getAsString()
						);
						newObj.addProperty(
								"duration", obj.get("Duration")
										.getAsString()
						);
						newObj.addProperty(
								"type", obj.get("Type")
										.getAsString()
						);
						newObj.addProperty(
								"sub_type", obj.get("SubType")
										.getAsString()
						);
						newObj.addProperty(
								"reason", obj.get("Reason")
										.getAsString()
						);

						newPresets.add(newObj);
					});

			result.add("presets", newPresets);
		} catch (Exception _) {
			JsonArray presetArray = getDefaultPresetsV1();
			result.add("presets", presetArray);
		}
	}

	@NonNullDecl
	private static JsonArray getDefaultPresetsV1() {
		JsonArray presetArray = new JsonArray();
		JsonObject pre1 = new JsonObject();
		pre1.addProperty("name", "Hacking");
		pre1.addProperty("duration", "30d");
		pre1.addProperty("type", "ban");
		pre1.addProperty("sub_type", "temp");
		pre1.addProperty("reason", "Hacking");
		JsonObject pre2 = new JsonObject();
		pre2.addProperty("name", "Hacking 2nd Offense");
		pre2.addProperty("duration", "3m");
		pre2.addProperty("type", "ban");
		pre2.addProperty("sub_type", "temp");
		pre2.addProperty("reason", "Hacking");
		presetArray.add(pre1);
		presetArray.add(pre2);
		return presetArray;
	}
}
