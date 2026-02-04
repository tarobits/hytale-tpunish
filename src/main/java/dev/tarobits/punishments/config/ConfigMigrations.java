package dev.tarobits.punishments.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.provider.ConfigProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigMigrations {
	private static final HytaleLogger LOGGER = TPunish.getLogger("ConfigMigrations");

	private static void log(String msg) {
		LOGGER.atInfo()
				.log(msg);
	}

	private static Map<Integer, Map<String, String>> createList() {
		Map<Integer, Map<String, String>> list = new Object2ObjectOpenHashMap<>();
		// Version 0 -> 1
		Map<String, String> version0to1 = new Object2ObjectOpenHashMap<>();
		version0to1.put("DoPunishmentLogs", "doPunishmentLogs");
		version0to1.put("ShowUpdateNotifications", "showUpdateNotifications");
		version0to1.put("Presets", "presets");
		list.put(1, version0to1);
		// Version 1 -> 2
		Map<String, String> version1to2 = new Object2ObjectOpenHashMap<>();
		version1to2.put("showUpdateNotifications", "doUpdateChecks");
		version1to2.put("doPunishmentLogs", "doLogging");
		list.put(2, version1to2);
		return list;
	}

	public static String migrateKey(
			Integer currentVersion,
			String key
	) {
		Map<Integer, Map<String, String>> migrationList = createList();
		String newKey = key;
		while (currentVersion < ConfigProvider.getConfigVersion()) {
			Map<String, String> versionMigrations = migrationList.computeIfAbsent(
					currentVersion, _ -> new Object2ObjectOpenHashMap<>());
			if (versionMigrations.isEmpty()) {
				currentVersion++;
				continue;
			}
			newKey = versionMigrations.getOrDefault(newKey, newKey);
			currentVersion++;
		}
		return newKey;
	}

	public static JsonObject performMigrations(
			JsonObject currentConfig,
			Integer currentVersion
	) {
		log("-=-=-= Running config migrations =-=-=-");
		JsonObject result = currentConfig.deepCopy();
		while (currentVersion < ConfigProvider.getConfigVersion()) {
			migrate(currentVersion, result);
			currentVersion++;
		}

		normalizeConfig(result);

		log("-=-=-= Config migrations END =-=-=-");
		return result;
	}

	private static void migrate(
			Integer currentVersion,
			JsonObject result
	) {
		switch (currentVersion) {
			case 0:
				zeroToOne(result);
				break;
			case 1:
				oneToTwo(result);
				break;
			// Add more migrations here
		}
	}

	// Clean config and add missing settings
	private static void normalizeConfig(
			JsonObject result
	) {
		for (ConfigSchema s : ConfigSchema.values()) {
			if (!result.has(s.getKey())) {
				ConfigEntry entry = s.getEntry();
				entry.parseValueToJson(result);
			}
		}
		List<String> keys = new ArrayList<>(result.keySet());
		for (String key : keys) {
			if (key.equals("_meta")) {
				continue;
			}
			if (!ConfigSchema.doesEntryExist(key)) {
				result.remove(key);
			}
		}
	}

	private static void oneToTwo(
			JsonObject result
	) {
		log("Running migration to config version 2");

		try {
			result.addProperty(
					"doUpdateChecks", result.get("showUpdateNotifications")
					.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doUpdateChecks", true);
		}

		try {
			result.addProperty(
					"doLogging", result.get("doPunishmentLogs")
					.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doLogging", true);
		}
	}

	private static void zeroToOne(
			JsonObject result
	) {
		log("Running migration to config version 1");
		try {
			result.addProperty(
					"doPunishmentLogs", result.get("DoPunishmentLogs")
							.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("doPunishmentLogs", true);
		}

		try {
			result.addProperty(
					"showUpdateNotifications", result.get("ShowUpdateNotifications")
							.getAsBoolean()
			);
		} catch (Exception _) {
			result.addProperty("showUpdateNotifications", true);
		}

		try {
			JsonArray newPresets = new JsonArray();
			result.get("Presets")
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
