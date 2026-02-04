package dev.tarobits.punishments.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.config.ConfigMigrations;
import dev.tarobits.punishments.config.ConfigSchema;
import dev.tarobits.punishments.storage.StorageUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class ConfigIdProvider extends BlockingDiskFile {
	private static final ConfigIdProvider INSTANCE = new ConfigIdProvider();
	private final Map<String, UUID> ids = new Object2ObjectOpenHashMap<>();

	protected ConfigIdProvider() {
		super(StorageUtils.createDataFile(
						TPunish.get()
								.getDataDirectory(), "configIds.json"
				)
				      .toPath());
		this.syncLoad();
	}

	public static ConfigIdProvider get() {
		return INSTANCE;
	}

	public UUID getUUIDForKey(String key) {
		UUID id = this.ids.computeIfAbsent(key, k -> UUID.randomUUID());
		this.syncSave();
		return id;
	}

	@Override
	protected void read(BufferedReader bufferedReader) throws IOException {
		try {
			JsonObject fullObject = JsonParser.parseReader(bufferedReader)
					.getAsJsonObject();

			int version = 0;
			try {
				version = fullObject.get("version")
						.getAsInt();
			} catch (Exception _) {

			}
			JsonArray array = fullObject.get("entries")
					.getAsJsonArray();

			for (JsonElement element : array) {
				JsonObject obj = element.getAsJsonObject();
				String key = obj.get("key")
						.getAsString();
				UUID id = UUID.fromString(obj.get("id")
						                          .getAsString());
				key = ConfigMigrations.migrateKey(version, key);
				if (ConfigSchema.doesEntryExist(key)) {
					ids.put(ConfigMigrations.migrateKey(version, key), id);
				}
			}
		} catch (Exception _) {
		}
	}

	@Override
	protected void write(BufferedWriter bufferedWriter) throws IOException {
		JsonObject fullObject = new JsonObject();

		fullObject.addProperty("version", ConfigProvider.getConfigVersion());
		JsonArray array = new JsonArray();
		for (String key : this.ids.keySet()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("key", key);
			obj.addProperty("id", this.ids.get(key)
					.toString()
			);
			array.add(obj);
		}
		fullObject.add("entries", array);
		bufferedWriter.write(fullObject.toString());
	}

	@Override
	protected void create(BufferedWriter bufferedWriter) throws IOException {
		try (JsonWriter writer = new JsonWriter(bufferedWriter)) {
			writer.beginObject();
			writer.endObject();
		}
	}
}
