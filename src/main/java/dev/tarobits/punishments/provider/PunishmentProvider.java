package dev.tarobits.punishments.provider;

import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.utils.ProviderState;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PunishmentProvider extends AbstractProvider<Punishment> {
	private static final PunishmentProvider INSTANCE = new PunishmentProvider();

	private final Map<PunishmentType, Integer> stats = new Object2ObjectOpenHashMap<>();
	private final Map<UUID, List<Punishment>> userPunishments = new Object2ObjectOpenHashMap<>();

	// Indexes
	private final Map<UUID, Punishment> activeBans = new Object2ObjectOpenHashMap<>();
	private final Map<UUID, Punishment> activeMutes = new Object2ObjectOpenHashMap<>();
	private final Map<UUID, List<Punishment>> warns = new Object2ObjectOpenHashMap<>();

	protected PunishmentProvider() {
		super("punishments.json", Punishment::fromJson);
		this.syncLoad();
		this.updateIndexes();
		String loadedString = "Successfully loaded " + this.stats.computeIfAbsent(
				PunishmentType.BAN,
				(_) -> 0
		) + " bans, " + this.stats.computeIfAbsent(
				PunishmentType.MUTE, (_) -> 0) + " mutes, " + this.stats.computeIfAbsent(
				PunishmentType.KICK,
				(_) -> 0
		) + " kicks and " + this.stats.computeIfAbsent(
				PunishmentType.WARN, (_) -> 0) + " warnings!";
		LOGGER.atInfo()
				.log(loadedString);
	}

	public static PunishmentProvider get() {
		return INSTANCE;
	}

	@Nullable
	public String userHasPunishment(
			UUID user,
			PunishmentType type
	) {
		if (!this.isReady()) {
			throw new IllegalArgumentException("Punishment Provider is not ready!");
		}
		return switch (type) {
			case BAN -> activeBans.containsKey(user) ? "ban" : null;
			case MUTE -> activeMutes.containsKey(user) ? "mute" : null;
			case KICK, WARN -> null;
		};
	}

	@Override
	public synchronized void addEntry(Punishment punishment) {
		if (!this.isReady()) {
			throw new IllegalArgumentException("Punishment Provider is not ready!");
		}
		this.entries.put(punishment.getId(), punishment);

		this.updateIndexes();

		this.syncSave();
	}

	protected synchronized void addFileEntry(Punishment punishment) {
		if (!this.isReading()) {
			throw new IllegalArgumentException("Punishment Provider cannot add FilePunishment if not reading!");
		}
		this.entries.put(punishment.getId(), punishment);
	}

	public void updateIndexes() {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		this.STATE = ProviderState.INDEX;
		this.activeBans.clear();
		this.activeMutes.clear();
		this.warns.clear();
		this.userPunishments.clear();
		this.stats.clear();
		this.entries.forEach((_, punishment) -> {
			this.stats.compute(punishment.getType(), (_, v) -> v == null ? 1 : v + 1);
			this.userPunishments.compute(
					punishment.getTarget(), (_, l) -> {
						List<Punishment> list = l;
						if (list == null) {
							list = new ArrayList<>();
						}
						list.add(punishment);
						return list;
					}
			);
			if (punishment.isActive() && !punishment.isPardoned()) {
				switch (punishment.getType()) {
					case BAN -> this.activeBans.put(punishment.getTarget(), punishment);
					case MUTE -> this.activeMutes.put(punishment.getTarget(), punishment);
					case WARN -> this.warns.computeIfAbsent(punishment.getTarget(), _ -> new ArrayList<>())
							.add(punishment);
				}
			}
		});
		this.STATE = ProviderState.READY;
	}

	public Punishment getActive(
			UUID user,
			PunishmentType type
	) {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		return switch (type) {
			case BAN -> this.activeBans.get(user);
			case MUTE -> this.activeMutes.get(user);
			default -> throw new IllegalArgumentException("Type does not have active entries!");
		};
	}

	public List<Punishment> getEntries(
			UUID user,
			PunishmentType type
	) {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		List<Punishment> punishmentList = new ArrayList<>();

		for (Punishment p : this.userPunishments.computeIfAbsent(user, _ -> new ArrayList<>())) {
			if (p.getType() == type) {
				punishmentList.add(p);
			}
		}
		return punishmentList;
	}

	public Boolean hasBan(UUID user) {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		return this.activeBans.get(user) != null;
	}

	public Boolean hasMute(UUID user) {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		return this.activeMutes.get(user) != null;
	}

	private Boolean punishmentExists(
			UUID user,
			UUID id
	) {
		return entries.values()
				.stream()
				.anyMatch((p) -> p.getId() == id && p.getTarget() == user);
	}

	public Boolean editPunishment(
			UUID user,
			UUID id,
			Function<Punishment, Boolean> modifier
	) {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		if (!this.punishmentExists(user, id)) {
			return false;
		}
		Boolean res = modifier.apply(this.entries.get(id));
		this.syncSave();
		this.updateIndexes();
		return res;
	}

	public Message updatePunishment(
			Punishment punishment,
			UUID user
	) throws InvalidActionException {
		if (!this.isReady()) {
			throw new DeveloperErrorException("Punishment Provider is not ready!");
		}
		if (!this.punishmentExists(user, punishment.getId())) {
			throw new InvalidActionException("tarobits.punishments.edit.error.doesntexist");
		}
		this.entries.put(punishment.getId(), punishment);
		this.updateIndexes();
		this.syncSave();
		return Message.translation("tarobits.punishments.edit.success");
	}
}
