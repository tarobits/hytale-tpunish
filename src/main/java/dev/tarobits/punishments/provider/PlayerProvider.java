package dev.tarobits.punishments.provider;

import dev.tarobits.punishments.domain.DomainObjectProvider;
import dev.tarobits.punishments.model.player.PlayerDomainObject;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerProvider implements DomainObjectProvider<PlayerDomainObject> {
	private static final PlayerProvider INSTANCE = new PlayerProvider();

	protected PlayerProvider() {

	}

	public static PlayerProvider get() {
		return INSTANCE;
	}

	private List<Permissions> getAllPermissions(UUID id) {
		List<Permissions> permissionList = new ArrayList<>();
		for (Permissions permission : Permissions.values()) {
			if (Permissions.playerHas(id, permission)) {
				permissionList.add(permission);
			}
		}
		return permissionList;
	}

	public PlayerDomainObject getFromId(UUID id) {
		String username = PlayerUtils.getUsername(id);
		return new PlayerDomainObject(id, username);
	}
}
