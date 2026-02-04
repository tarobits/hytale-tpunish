package dev.tarobits.punishments.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.utils.config.ConfigSchema;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VersionChecker implements Runnable {
	private static final HytaleLogger logger = TPunish.getLogger(VersionChecker.class.getSimpleName());
	private static final ConfigProvider configProvider = ConfigProvider.get();
	private final Semver currentVersion;

	public VersionChecker(Semver currentVersion) {
		this.currentVersion = currentVersion;
	}

	public static Message checkVersions(Semver current) {
		try {
			Semver newest = getNewestVersion();

			if (newest.compareTo(current) > 0) {

				logger.atInfo()
						.log("Found new version!\nRelease channel:" + (configProvider
								.getFromSchema(ConfigSchema.DEVELOPMENT_RELEASE)
								.getAsBoolean() ? "Development" : "Stable") + "\nCurrent version: " + current + "\nNewest version: " + newest);
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

	private static Semver getNewestVersion() throws UserException {
		try {
			String res = "";
			HttpClient client = HttpClient.newHttpClient();
			if (configProvider.getFromSchema(ConfigSchema.DO_METRICS)
					.getAsBoolean()) {

				String data = "releaseChannel=" + (configProvider.getFromSchema(ConfigSchema.DEVELOPMENT_RELEASE)
						.getAsBoolean() ? "beta" : "stable") + "&version=" + TPunish.get()
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
				URI uri = URI.create("https://pluginver.tarobits.workers.dev" + (configProvider.getFromSchema(
								ConfigSchema.DEVELOPMENT_RELEASE)
						.getAsBoolean() ? "/?releaseChannel=beta" : ""));
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

	@Override
	public void run() {
		if (!configProvider.getFromSchema(ConfigSchema.DO_UPDATE_CHECKS)
				.getAsBoolean()) {
			return;
		}
		logger.atInfo()
				.log("Running automatic update check");

		Message updateNotif = checkVersions(currentVersion);
		if (updateNotif == null) {
			logger.atInfo()
					.log("No updates found");
			return;
		}
		for (PlayerRef plr : Universe.get()
				.getPlayers()) {
			if (Permissions.playerHas(plr.getUuid(), Permissions.CONFIG)) {
				plr.sendMessage(updateNotif);
			}
		}
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

	public void schedule(ScheduledExecutorService scheduledExecutorService) {
		Integer frequency = configProvider.getFromSchema(ConfigSchema.UPDATE_CHECK_FREQUENCY)
				.getAsInteger();
		if (frequency == 0) {
			throw new DeveloperErrorException("Frequency is 0!");
		}
		scheduledExecutorService.scheduleAtFixedRate(this, 0, frequency, TimeUnit.HOURS);
	}
}
