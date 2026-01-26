# TPunish
![License Badge](https://img.shields.io/badge/License-LGPL--3.0--or--later-orange)
![CurseForge Version](https://img.shields.io/curseforge/v/1445040?label=Version)

This plugin is an advanced punishment management plugin for Hytale.

> [!NOTE]
> This plugin overrides the vanilla /ban, /kick and /unban commands

## Table of contents
- [Quick Start](#quick-start)
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

```json
{
  "ShowUpdateNotifications": true,
  "Presets":[
    {
      "Name": "[NAME THAT IS DISPLAYED]",
      "Type": "[ban|mute|kick|warn]",
      "SubType": "[temp|perm] only needed for ban and mute",
      "Duration": "[DURATION STRING] only needed for subtype temp",
      "Reason": "[REASON WHY THE ACTION IS BEING PERFORMED]"
    }
  ]
}
```

## Features

- Punishment UI
- Punishment presets
- Many different punishment types
  - Bans
  - Mutes
  - Kicks
  - Warnings

### Planned

- Localization
- Reduction of punishments
- Custom chat / kick messages
- Appeal process
- Punishment history log

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

`/punish` => Opens the GUI

`/punish config` => Opens the config GUI

`/ban <player> <duration> <reason>` => Ban a player

`/mute <player> <duration> <reason>` => Mute a player

`/kick <player> <reason>` => Kick a player

`/warn <player> <reason>` => Warn a player

`/unban <player>` => Unban a player

`/unmute <player>` => Unmute a player

`<player>` => If the player is online the start of the username is enough. If the player is offline you must type the full username

`<duration>` => A duration string consisting of the following variables with no spaces
```
y => year
m => month
d => day
h => hour
min => minute

perm => permanent ban
```

`<reason>` => The reason why the action is being performed

## End notes

If you'd like to support this project or any of my other ones you may do so with this link.

[![Static Badge](https://img.shields.io/badge/Buy%20me%20a%20coffee-Tarobits-brown?style=for-the-badge&logo=buymeacoffee&logoColor=white&labelColor=yellow&color=%236F4E37)](https://buymeacoffee.com/tarobits)