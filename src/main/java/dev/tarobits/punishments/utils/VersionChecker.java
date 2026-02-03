package dev.tarobits.punishments.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.utils.config.ConfigSchema;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionChecker {

	public static Message checkVersions(Semver current) {
		try {
			Semver newest = getNewestVersion();

			HytaleLogger logger = TPunish.getLogger(VersionChecker.class.getSimpleName());

			if (newest.compareTo(current) > 0) {

				logger.atInfo()
						.log("Found new version!\nRelease channel:" + ((boolean) ConfigProvider.get()
								.getFromSchema(ConfigSchema.DEVELOPMENT_RELEASE)
								.getValue() ? "Development" : "Stable") + "\nCurrent version: " + current + "\nNewest version: " + newest);
				return getUpdateMessage(newest, current);
			} else if (newest.compareTo(current) < 0) {
				logger.atWarning()
						.log("You seem to be using a development release and\nyour update notifications are set to the stable channel.\nTo change this set " + ConfigSchema.DEVELOPMENT_RELEASE.getKey() + " to true in your config.json.");
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
			String res = "";
			HttpClient client = HttpClient.newHttpClient();
			ConfigProvider provider = ConfigProvider.get();
			if ((boolean) provider.getFromSchema(ConfigSchema.DO_METRICS)
					.getValue()) {

				String data = "releaseChannel=" + ((boolean) provider.getFromSchema(ConfigSchema.DEVELOPMENT_RELEASE)
						.getValue() ? "beta" : "stable") + "&version=" + TPunish.get()
						.getVersion()
						.toString();
				URI uri = URI.create("https://pluginver.tarobits.workers.dev");
				HttpRequest request = HttpRequest.newBuilder()
						.uri(uri)
						.header("Content-Type", "application/x-www-form-urlencoded")
						.POST(HttpRequest.BodyPublishers.ofString(data))
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() != 200) {
					throw new UserException("Failed to get new version!");
				}

				res = response.body();
			} else {
				URI uri = URI.create("https://pluginver.tarobits.workers.dev" + ((boolean) provider.getFromSchema(
								ConfigSchema.DEVELOPMENT_RELEASE)
						.getValue() ? "/?releaseChannel=beta" : ""));
				HttpRequest request = HttpRequest.newBuilder()
						.uri(uri)
						.GET()
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() != 200) {
					throw new UserException("Failed to get new version!");
				}
				res = response.body();
			}

			JsonObject obj = JsonParser.parseString(res)
					.getAsJsonObject();

			return Semver.fromString(
					obj.get("version")
							.getAsString(), true
			);
		} catch (Exception _) {
			throw new UserException("Couldn't get version!");
		}
	}
}
