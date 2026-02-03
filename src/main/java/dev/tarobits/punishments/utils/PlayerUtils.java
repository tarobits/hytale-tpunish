package dev.tarobits.punishments.utils;

import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;

import java.util.UUID;

public class PlayerUtils {
	public static final UUID consoleId = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public static String getUsername(UUID actor) {
		if (actor.toString()
				.equals("00000000-0000-0000-0000-000000000000")) {
			return "Console";
		}
		ServerAuthManager authManager = ServerAuthManager.getInstance();
		String sessionToken = authManager.getSessionToken();

		if (sessionToken == null) {
			return actor.toString();
		}

		ProfileServiceClient profileServiceClient = authManager.getProfileServiceClient();
		var profile = profileServiceClient.getProfileByUuid(actor, sessionToken);
		if (profile == null) {
			throw new DeveloperErrorException("Could not find player by uuid: " + actor);
		}
		return profile.getUsername();
	}
}
