package dev.tarobits.punishments.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.model.punishment.PunishmentSubtype;
import dev.tarobits.punishments.model.punishment.PunishmentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public enum Permissions {
	OPEN_GUI("gui", null),
	BAN_COMMAND("ban", null),
	BAN_TEMP("ban", "temp", PunishmentType.BAN, PunishmentSubtype.TEMPORARY),
	BAN_PERM("ban", "perm", PunishmentType.BAN, PunishmentSubtype.PERMANENT),
	UNBAN("unban", null),
	MUTE_COMMAND("mute", null),
	MUTE_TEMP("mute", "temp", PunishmentType.MUTE, PunishmentSubtype.TEMPORARY),
	MUTE_PERM("mute", "perm", PunishmentType.MUTE, PunishmentSubtype.PERMANENT),
	UNMUTE("unmute", null),
	WARN("warn", null, PunishmentType.WARN, PunishmentSubtype.NULL),
	WARN_COMMAND("warn", "custom"),
	UNWARN("unwarn", null),
	KICK("kick", null, PunishmentType.KICK, PunishmentSubtype.NULL),
	KICK_COMMAND("kick", "custom"),
	CUSTOM("punish", "custom"),
	CONFIG("config", null);


	private static final String base = "tpunish.";
	private final String root;
	@Nullable
	private final String sub;
	private final String permission;
	@Nullable
	private final PunishmentType type;
	@Nullable
	private final PunishmentSubtype subtype;

	Permissions(
			@Nonnull String root,
			@Nullable String sub
	) {
		String tempFull = base;
		tempFull += root;
		if (sub != null) {
			tempFull += "." + sub;
		}
		this.permission = tempFull;
		this.root = root;
		this.sub = sub;
		this.type = null;
		this.subtype = null;
	}

	Permissions(
			@Nonnull String root,
			@Nullable String sub,
			@Nullable PunishmentType type,
			@Nullable PunishmentSubtype subtype
	) {
		String tempFull = base;
		tempFull += root;
		if (sub != null) {
			tempFull += "." + sub;
		}
		this.permission = tempFull;
		this.root = root;
		this.sub = sub;
		this.type = type;
		this.subtype = subtype;
	}

	public static List<Permissions> getPermissionsFromType(@Nonnull PunishmentType type) {
		List<Permissions> newList = new ArrayList<>();
		for (Permissions p : Permissions.values()) {
			if (p.type == type) {
				newList.add(p);
			}
		}
		return newList;
	}

	public static Permissions getPermissionFromTypes(
			@Nonnull PunishmentType type,
			@Nonnull PunishmentSubtype subtype
	) {
		for (Permissions p : Permissions.values()) {
			if (p.type == type && p.subtype == subtype) {
				return p;
			}
		}
		throw new DeveloperErrorException(
				"Permission for type " + type.toDisplayString() + " subtype " + subtype.toDisplayString() + " does not exist!");
	}

	@Nullable
	public static Permissions getByTranslationKey(
			@Nonnull String root,
			@Nonnull String sub
	) {
		if (sub.isEmpty()) {
			sub = null;
		}
		for (Permissions p : Permissions.values()) {
			if (Objects.equals(p.root, root) && Objects.equals(p.sub, sub)) {
				return p;
			}
		}
		return null;
	}

	public static Boolean playerHas(
			@Nonnull UUID uuid,
			@Nonnull Permissions permission
	) {
		if (uuid.equals(PlayerUtils.consoleId)) {
			return true;
		}
		return PermissionsModule.get()
				.hasPermission(uuid, permission.getPermission());
	}

	public static Boolean playerHasAny(
			@Nonnull UUID uuid,
			@Nonnull List<Permissions> permissions
	) {
		if (uuid.equals(PlayerUtils.consoleId)) {
			return true;
		}
		return permissions.stream()
				.anyMatch((p) -> PermissionsModule.get()
						.hasPermission(uuid, p.getPermission()));
	}

	public String getPermission() {
		return this.permission;
	}

	public Boolean hasPermission(Player player) {
		return player.hasPermission(this.getPermission());
	}
}
