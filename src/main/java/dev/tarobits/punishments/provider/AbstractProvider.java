package dev.tarobits.punishments.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.storage.StorageUtils;
import dev.tarobits.punishments.utils.ProviderState;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.domainobject.OwnerRole;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

abstract public class AbstractProvider<T extends DomainObject<T>> extends BlockingDiskFile {
	protected static HytaleLogger LOGGER;
	protected final Function<JsonObject, T> loader;
	protected final Map<UUID, T> entries = new Object2ObjectOpenHashMap<>();
	protected ProviderState STATE = ProviderState.INIT;

	protected AbstractProvider(
			String fileName,
			Function<JsonObject, T> loader
	) {
		Path pluginDir = TPunish.get()
				.getDataDirectory();
		super(StorageUtils.createDataFile(pluginDir, fileName)
				      .toPath());
		LOGGER = TPunish.getLogger(this.getClass()
				                           .getSimpleName());
		this.loader = loader;
	}

	protected AbstractProvider(
			String fileName,
			Function<JsonObject, T> loader,
			Boolean storage
	) {
		Path pluginDir = TPunish.get()
				.getDataDirectory();
		super(StorageUtils.createDataFile(pluginDir, fileName, storage)
				      .toPath());
		LOGGER = TPunish.getLogger(this.getClass()
				                           .getSimpleName());
		this.loader = loader;
	}

	protected T ensureUniqueId(T entry) throws InvalidActionException {
		UUID id = entry.getId();
		if (!this.entries.containsKey(id)) {
			return entry;
		}

		LOGGER.atWarning()
				.log("Entry id " + entry.getId() + " already exists! Giving new id!");

		return entry.withId(UUID.randomUUID());
	}

	// Get all entries that have the Target of UUID with the type ownerType
	public List<T> getAllTargetIs(
			DomainObjectType ownerType,
			UUID id
	) {
		return this.entries.values()
				.stream()
				.filter((e) -> e.getOwners()
						.get(OwnerRole.TARGET)
						.type() == ownerType && e.getOwners()
						.get(OwnerRole.TARGET)
						.id()
						.equals(id))
				.toList();
	}

	// Get all entries that have the Actor of UUID with the type ownerType
	public List<T> getAllActorIs(
			DomainObjectType ownerType,
			UUID id
	) {
		return this.entries.values()
				.stream()
				.filter((e) -> e.getOwners()
						.get(OwnerRole.ACTOR)
						.type() == ownerType && e.getOwners()
						.get(OwnerRole.ACTOR)
						.id()
						.equals(id))
				.toList();
	}

	public List<T> getAllRelatedIs(
			DomainObjectType ownerType,
			UUID id
	) {
		List<T> list = new ArrayList<>(getAllTargetIs(ownerType, id));
		list.addAll(getAllActorIs(ownerType, id));
		return list;
	}

	public T getFromId(UUID id) {
		if (!this.isReady()) {
			throw new IllegalArgumentException("Provider is not ready!");
		}
		if (!this.entries.containsKey(id)) {
			throw new IllegalArgumentException("Entry with id " + id + " does not exist!");
		}
		return this.entries.get(id);
	}

	abstract public void addEntry(T entry);

	abstract protected void addFileEntry(T entry);

	protected Boolean canFileAction() {
		return this.STATE == ProviderState.INIT || this.STATE == ProviderState.READY;
	}

	protected Boolean isReading() {
		return this.STATE == ProviderState.READ;
	}

	protected Boolean isReady() {
		return this.STATE == ProviderState.READY;
	}

	@Override
	protected synchronized void read(BufferedReader bufferedReader) {
		if (!this.canFileAction()) {
			throw new IllegalArgumentException("Punishment provider cannot read!");
		}
		this.STATE = ProviderState.READ;
		JsonParser.parseReader(bufferedReader)
				.getAsJsonArray()
				.forEach((e) -> {
					JsonObject jsonObject = e.getAsJsonObject();

					try {
						T entry = this.loader.apply(jsonObject);
						this.addFileEntry(this.ensureUniqueId(entry));
					} catch (Exception ex) {
						throw new RuntimeException("Failed to parse!", ex);
					}
				});
		this.STATE = ProviderState.READY;
	}

	@Override
	protected synchronized void write(BufferedWriter bufferedWriter) throws IOException {
		if (!this.canFileAction()) {
			throw new IllegalArgumentException("Punishment provider cannot write!");
		}
		this.STATE = ProviderState.WRITE;
		JsonArray array = new JsonArray();
		this.entries.forEach((_, value) -> array.add(value.toJsonObject()));
		bufferedWriter.write(array.toString());
		this.STATE = ProviderState.READY;
	}

	@Override
	protected synchronized void create(BufferedWriter bufferedWriter) throws IOException {
		try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
			jsonWriter.beginArray()
					.endArray();
		}
	}
}
