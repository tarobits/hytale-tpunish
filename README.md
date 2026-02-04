# TPunish
![License Badge](https://img.shields.io/badge/License-LGPL--3.0--or--later-orange)
![Plugin Version](https://img.shields.io/endpoint?url=https%3A%2F%2Fpluginver.tarobits.workers.dev%2F%3Fbadge%3Db)
![Beta Version](https://img.shields.io/endpoint?url=https%3A%2F%2Fpluginver.tarobits.workers.dev%2F%3Fbadge%3Dh%26releaseChannel%3Dbeta)

This plugin is an advanced punishment management plugin for Hytale.

> [!NOTE]
> This plugin overrides the vanilla /ban, /kick and /unban commands

> [!NOTE]
> This plugin collects anonymized usage metrics when searching for updates by default.
>
> These metrics consist of installed plugin version and selected release channel.
>
> They are immediately aggregated per version and per release channel.
>
> The raw data is never saved.
>
> Your ip address, any sort of identifier, etc. are never processed, stored or evaluated.
>
> To disable this set `doMetrics` to false in the `config.json`

## Table of contents
- [Quick Start](#quick-start)
- [Configuration](#configuration)
  - [Preset value](#preset-value)
  - [Duration string](#duration-string) 
- [Features](#features)
  - [Planned features](#planned)
- [Permissions](#permissions)
- [Command Syntax](#command-syntax)

## Quick Start

1. Download the latest release from [Curseforge](https://curseforge.com/hytale/mods/tpunish)
2. Put it in your servers `mods` folder
3. Start the server to generate an example config
4. Edit the config in `mods/Tarobits_Punishments/config.json` (See [Configuration](#configuration) for more details)

## Configuration

|         Key          |               Value               |                                          Function                                          |        Default         |
|:--------------------:|:---------------------------------:|:------------------------------------------------------------------------------------------:|:----------------------:|
|    doUpdateChecks    |       boolean (true/false)        |                               Enable automatic update checks                               |          true          |
| updateCheckFrequency |              integer              |         How often update checks are performed (in hours) <br/>Set to 0 to disable          |           2            |
|      doLogging       |       boolean (true/false)        |                                 Enable logging of actions                                  |          true          |
|      doMetrics       |       boolean (true/false)        | Enable anonymized metric collection while fetching updates (pluginVersion, releaseChannel) |          true          |
|  developmentRelease  |       boolean (true/false)        |                     Change update notifications to development channel                     |         false          |
|       presets        | array of [Presets](#preset-value) |                                Presets for the punishments                                 | predefined punishments |

### Preset value

|   Key    |                Value                |                               Function                               |           Required            |
|:--------:|:-----------------------------------:|:--------------------------------------------------------------------:|:-----------------------------:|
|   name   |               string                |               Name that is displayed to the moderators               |            always             |
|   type   |      enum(ban,mute,kick,warn)       |                        Type of the punishment                        |            always             |
| sub_type |     enum(temp,perm) or omitted      |                      SubType of the punishment                       | only when type is ban or mute |
| duration | [Duration string](#duration-string) |                    The duration of the punishment                    |  only when sub_type is temp   |
|  reason  |               string                | Reason displayed to the player when this punishment is given to them |            always             |

### Duration string

**Format:** (int)(y,m,d,h,min)*?
**Examples:** 
- 12y2m3d4h6min
- 5m

| Key | Function |
|:---:|:--------:|
|  y  |  years   |
|  m  |  months  |
|  d  |   days   |
|  h  |  hours   |
| min | minutes  |

> [!WARNING]
> m is for months not minutes


## Features

- Punishment UI
- Player details
  - Past punishments
- Punishment presets
- Many different punishment types
  - Bans
  - Mutes
  - Kicks
  - Warnings

### Planned

- Localization
- Reduction and extension of punishments
- Customization of [Duration string parameters](#duration-string)
- Customization of display (Including but not limited to:)
  -  Custom message format (disconnection, chat message, etc.)
  -  Email format
- In-game appeal process (with optional email support)
- In-game configuration
- Logging of actions performed
- Broadcasted or silent punishments (Currently all punishments are silent)
- Player details
  - Chat log
- Database support (primarily for big servers with multiple sub-servers)

## Permissions

- `tpunish.gui` Open the punishment gui
- `tpunish.config` Open the in-game config gui and receive update notifications if enabled
- `tpunish.ban` Access the `/ban` command (Enables access to custom punishments)
- `tpunish.ban.temp` Create temporary bans
- `tpunish.ban.perm` Create permanent bans
- `tpunish.unban` Unban users
- `tpunish.mute` Access the `/mute` command (Enables access to custom punishments)
- `tpunish.mute.temp` Create temporary mutes
- `tpunish.mute.perm` Create permanent mutes
- `tpunish.unmute` Unmute users
- `tpunish.kick` Kick users
- `tpunish.kick.custom` Access to the `/kick` command (Enables access to custom punishments)
- `tpunish.warn` Warn users
- `tpunish.warn.custom` Access to the `/warn` command (Enables access to custom punishments)
- `tpunish.custom` Access custom punishments inside the punishment gui (Not implemented)

## Command Syntax

> [!NOTE]
> All parameters shown are required

`/punish` => Opens the GUI

`/tpconfig` or `/tpc` => Opens the config GUI

`/tpconfig reload` => Reloads config.json

`/tpconfig version` => Displays current version

`/tpconfig checkversion` => Checks for updates manually

`/ban [player] [duration] [reason]` => Ban a player

`/mute [player] [duration] [reason]` => Mute a player

`/kick [player] [reason]` => Kick a player

`/warn [player] [reason]` => Warn a player

`/unban [player]` => Unban a player

`/unmute [player]` => Unmute a player

`[player]` => If the player is online the start of the username is enough. If the player is offline you must type the full username

`[duration]` => [Duration string](#duration-string) or perm (for a permanent action)

`[reason]` => The reason why the action is being performed

## End notes

If you'd like to support this project or any of my other ones you may do so with this link.

[![Static Badge](https://img.shields.io/badge/Buy%20me%20a%20coffee-Tarobits-brown?style=for-the-badge&logo=buymeacoffee&logoColor=white&labelColor=yellow&color=%236F4E37)](https://buymeacoffee.com/tarobits)
