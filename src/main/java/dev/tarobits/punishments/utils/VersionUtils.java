package dev.tarobits.punishments.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.exceptions.UserException;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class VersionUtils {

	public static Message checkVersions(Semver current) {
		try {
			Semver newest = getNewestVersion();

			if (newest.compareTo(current) > 0) {
				return getUpdateMessage(newest, current);
			}
		} catch (UserException e) {
			return e.getChatMessage();
		}
		return null;
	}

	private static Message getUpdateMessage(
			Semver newest,
			Semver current
	) {
		return Message.translation("tarobits.punishments.updatemessage")
				.param(
						"newVersion", Message.raw(newest.toString())
								.color(Color.GREEN)
								.bold(true)
				)
				.param(
						"oldVersion", Message.raw(current.toString())
								.color(Color.RED)
								.bold(true)
				);
	}

	private static Semver getNewestVersion() throws UserException {
		try {
			URL manifest = URI.create(
							"https://raw.githubusercontent.com/tarobits/hytale-tpunish/refs/heads/main/src/main/resources/manifest.json")
					.toURL();
			StringBuilder read = new StringBuilder();
			Scanner scanner = new Scanner(manifest.openStream());

			while (scanner.hasNext()) {
				read.append(scanner.nextLine());
			}

			scanner.close();

			JsonObject obj = JsonParser.parseString(read.toString())
					.getAsJsonObject();

			return Semver.fromString(
					obj.get("Version")
							.getAsString(), true
			);
		} catch (Exception _) {
			throw new UserException("Couldn't get version!");
		}
	}
}
