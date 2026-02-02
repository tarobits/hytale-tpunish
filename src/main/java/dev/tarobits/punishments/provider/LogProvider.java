package dev.tarobits.punishments.provider;

import dev.tarobits.punishments.utils.domainobject.DomainObjectType;
import dev.tarobits.punishments.utils.log.LogEntry;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LogProvider extends AbstractProvider<LogEntry> {
	private static final LogProvider INSTANCE = new LogProvider();

	protected LogProvider() {
		super("logs.json", LogEntry::fromJson);
		this.syncLoad();
		this.syncSave();
	}

	public static LogProvider get() {
		return INSTANCE;
	}

	private List<LogEntry> sortLogs(@Nonnull List<LogEntry> logEntries) {
		return logEntries.stream()
				.sorted(Comparator.comparingLong((LogEntry o) -> o.getTimestamp()
								.toEpochMilli())
						        .reversed())
				.toList();
	}

	@Override
	public List<LogEntry> getAllTargetIs(
			DomainObjectType ownerType,
			UUID id
	) {
		return sortLogs(super.getAllTargetIs(ownerType, id));
	}

	@Override
	public List<LogEntry> getAllActorIs(
			DomainObjectType ownerType,
			UUID id
	) {
		return sortLogs(super.getAllActorIs(ownerType, id));
	}

	@Override
	public List<LogEntry> getAllRelatedIs(
			DomainObjectType ownerType,
			UUID id
	) {
		return sortLogs(super.getAllRelatedIs(ownerType, id));
	}

	@Override
	public void addEntry(LogEntry logEntry) {
		this.entries.put(logEntry.getId(), logEntry);
		this.syncSave();
	}

	@Override
	public void addFileEntry(LogEntry logEntry) {
		this.entries.put(logEntry.getId(), logEntry);
	}

	public Boolean checkIfTargetTypeExists(
			String action,
			UUID id,
			DomainObjectType ownerType
	) {
		return getAllTargetIs(ownerType, id).stream()
				.anyMatch(logEntry -> Objects.equals(logEntry.getAction(), action));
	}
}
