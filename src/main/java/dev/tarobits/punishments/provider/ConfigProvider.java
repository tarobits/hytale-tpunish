package dev.tarobits.punishments.provider;

import com.google.gson.FormattingStyle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.utils.ProviderState;
import dev.tarobits.punishments.utils.config.ConfigEntry;
import dev.tarobits.punishments.utils.config.ConfigMigrations;
import dev.tarobits.punishments.utils.config.ConfigSchema;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

public class ConfigProvider extends AbstractProvider<ConfigEntry> {
	private static final ConfigProvider INSTANCE = new ConfigProvider();

	private static final int VERSION = 1;

	protected ConfigProvider() {
		super("config.json", ConfigEntry::fromJson, false);

		this.syncLoad();

		this.syncSave();
	}

	public static ConfigProvider get() {
		return INSTANCE;
	}

	public static int getConfigVersion() {
		return VERSION;
	}

	public ConfigEntry getFromKey(String key) {
		return this.getFromId(ConfigSchema.getEntry(key)
				                      .getId());
	}

	public ConfigEntry getFromSchema(ConfigSchema schema) {
		return this.getFromKey(schema.getKey());
	}

	@Override
	public void addEntry(ConfigEntry entry) {
		throw new IllegalArgumentException("Config entries cannot be added!");
	}

	@Override
	protected void addFileEntry(ConfigEntry entry) {
		throw new IllegalArgumentException("Config entries cannot be added!");
	}

	@Override
	protected void read(BufferedReader bufferedReader) {
		if (!this.canFileAction()) {
			throw new IllegalArgumentException("Punishment provider cannot read!");
		}
		this.STATE = ProviderState.READ;
		LOGGER.atInfo()
				.log("-=-=-= Reading config =-=-=-");
		JsonObject obj = JsonParser.parseReader(bufferedReader)
				.getAsJsonObject();
		JsonObject meta;
		int loadedVersion;
		try {
			meta = obj.get("_meta")
					.getAsJsonObject();
			loadedVersion = meta.get("version")
					.getAsInt();
		} catch (Exception _) {
			LOGGER.atInfo()
					.log("Failed to get config meta. Assuming pre-version 1 config");
			loadedVersion = 0;
		}
		obj = ConfigMigrations.performMigrations(obj, loadedVersion);
		this.entries.clear();

		Collection<ConfigEntry> schema = ConfigSchema.getAllEntries();

		for (ConfigEntry e : schema) {
			LOGGER.at(Level.FINEST)
					.log("Reading config key " + e.getKey());
			try {
				e.parseValueFromJson(obj.get(e.getKey()));
				this.entries.put(e.getId(), e);
			} catch (Exception f) {
				try {
					e.setValue(e.getDefaultValue());
				} catch (InvalidActionException d) {
					throw new DeveloperErrorException(d.getMessage());
				}
				if (f instanceof InvalidActionException) {
					LOGGER.atWarning()
							.log(f.getMessage());
				}
				this.entries.put(e.getId(), e);
				LOGGER.at(Level.FINEST)
						.log("Failed to get config key " + e.getKey() + " using default");
			}
		}

		LOGGER.atInfo()
				.log("-=-=-= Reading config END =-=-=-");
		this.STATE = ProviderState.READY;
	}

	@Override
	protected void write(BufferedWriter bufferedWriter) throws IOException {
		if (!this.canFileAction()) {
			throw new IllegalArgumentException("Punishment provider cannot write!");
		}
		LOGGER.at(Level.FINEST)
				.log("-=-=-= Writing config =-=-=-");
		this.STATE = ProviderState.WRITE;
		JsonObject obj = new JsonObject();
		JsonObject meta = new JsonObject();
		meta.addProperty("version", VERSION);

		obj.add("_meta", meta);
		for (ConfigEntry entry : this.entries.values()) {
			entry.parseValueToJson(obj);
		}
		StringBuilder jsonString = new StringBuilder();
		JsonWriter jsonWriter = new JsonWriter(Streams.writerForAppendable(jsonString));
		jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
		jsonWriter.setStrictness(Strictness.STRICT);
		Streams.write(obj, jsonWriter);
		bufferedWriter.write(jsonString.toString());
		LOGGER.at(Level.FINEST)
				.log("-=-=-= Writing config END =-=-=-");
		this.STATE = ProviderState.READY;
	}

	@Override
	protected void create(BufferedWriter bufferedWriter) throws IOException {
		try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
			jsonWriter.beginObject()
					.endObject();
		}
	}

	public void saveConfig() {
		this.syncSave();
	}

	public void loadConfig() {
		this.syncLoad();
	}

	public void updateEntry(
			ConfigSchema schema,
			Object value
	) throws InvalidActionException {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Config provider is not ready!");
		}
		ConfigEntry old = this.entries.get(schema.getEntry()
				                                   .getId());
		old.setValue(value);
		this.entries.put(
				schema.getEntry()
						.getId(), old
		);
		this.syncSave();
	}
}
