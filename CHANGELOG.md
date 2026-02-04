# Changelog

## Version 0.3.0-pre2

> [!NOTE]
> Pre-release version! Could contain major bugs

### Added

- Command `/tpconfig` or `/tpc` now replaces `/punish config`
- Command `/tpconfig reload` to reload the config file while the server is running
- Command `/tpconfig version` to display the current version
- Command `/tpconfig checkversion` to check for updates manually
- Add new release channel
- Config option to opt-out of anonymized usage metrics
- Config option `updateCheckFrequency` to set how often to do update checks in addition to the one on start up
- Changelog

### Changed

- Config option `doPunishmentLogs` renamed to `doLogging`
- Config option `showUpdateNotifications` renamed to `doUpdateChecks`

### Fixed

- Prevent no permission message when executing commands from the console

### Removed

- Command `/punish config` due to it being replaced by `/tpconfig`

## Version 0.3.0-pre1

> [!NOTE]
> Pre-release version! Could contain major bugs

### Added

- Logging (currently only for punishments)
- Details page (currently only for punishments)
- Config migration