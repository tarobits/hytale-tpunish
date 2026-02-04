package dev.tarobits.punishments;

import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.tarobits.punishments.commands.*;
import dev.tarobits.punishments.config.ConfigSchema;
import dev.tarobits.punishments.model.punishment.PunishmentType;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.VersionChecker;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class TPunish extends JavaPlugin {
	private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
	private static TPunish INSTANCE;
	private static ConfigProvider configProvider;
	private static PunishmentProvider punishmentProvider;
	private static VersionChecker versionChecker;
	private final ScheduledExecutorService threadPool;

	private Boolean commandsRegistered = false;

	public TPunish(@Nonnull JavaPluginInit init) {
		super(init);
		INSTANCE = this;

		this.threadPool = Executors.newScheduledThreadPool(
				4, r -> {
					Thread thread = new Thread(r);
					thread.setName("TPunish-Thread-" + thread.threadId());
					return thread;
				}
		);

		versionChecker = new VersionChecker(this.getVersion());
		LOGGER.atInfo()
				.log("TPunish (Version " + this
						.getVersion()
						.toString() + ")");
	}

	public static HytaleLogger getLogger(String name) {
		return LOGGER.getSubLogger(name);
	}

	public static TPunish get() {
		return INSTANCE;
	}

	public Semver getVersion() {
		return this.getManifest()
				.getVersion();
	}

	private CompletableFuture<PlayerChatEvent> handleChat(CompletableFuture<PlayerChatEvent> future) {
		return future.thenApply(event -> {
			PlayerRef playerRef = event.getSender();
			UUID uuid = playerRef.getUuid();
			if (punishmentProvider.hasMute(uuid)) {
				playerRef.sendMessage(punishmentProvider.getActive(uuid, PunishmentType.MUTE)
						                      .getReasonMessage());
				event.setCancelled(true);
			}
			return event;
		});
	}

	protected void registerCommands() {
		this.getCommandRegistry()
				.registerCommand(new TPConfigCommand());
		this.getCommandRegistry()
				.registerCommand(new BanCommand());
		this.getCommandRegistry()
				.registerCommand(new UnbanCommand());
		this.getCommandRegistry()
				.registerCommand(new MuteCommand());
		this.getCommandRegistry()
				.registerCommand(new UnmuteCommand());
		this.getCommandRegistry()
				.registerCommand(new WarnCommand());
		this.getCommandRegistry()
				.registerCommand(new PunishCommand());
		this.getCommandRegistry()
				.registerCommand(new KickCommand());
		LOGGER.atInfo()
				.log("Successfully registered commands!");
		this.commandsRegistered = true;
	}

	protected void registerEvents() {
		// On boot register commands and check version if enabled
		this.getEventRegistry()
				.register(
						BootEvent.class, (_) -> {
							if (!this.commandsRegistered) {
								this.registerCommands();
							}
							if (configProvider.getFromSchema(
											ConfigSchema.DO_UPDATE_CHECKS)
									.getAsBoolean()) {
								versionChecker.run();
							}
						}
				);
		// When player attempts to connect check if ban is active
		this.getEventRegistry()
				.register(
						PlayerSetupConnectEvent.class, (event) -> {
							if (punishmentProvider.hasBan(event.getUuid())) {
								event.setReason(punishmentProvider.getActive(event.getUuid(), PunishmentType.BAN)
										                .getReasonMessage()
										                .getAnsiMessage());
								event.setCancelled(true);
							}
						}
				);
		// When player chats check if mute is active
		this.getEventRegistry()
				.registerAsyncGlobal(PlayerChatEvent.class, this::handleChat);
	}

	protected void scheduleVersionChecker() {
		if (configProvider.getFromSchema(ConfigSchema.DO_UPDATE_CHECKS)
				.getAsBoolean() && configProvider.getFromSchema(ConfigSchema.UPDATE_CHECK_FREQUENCY)
				.getAsInteger() != 0) {
			versionChecker.schedule(threadPool);
		}
	}

	protected void initializeProviders() {
		configProvider = ConfigProvider.get();
		punishmentProvider = PunishmentProvider.get();
		Object _ = LogProvider.get();
		LOGGER.atInfo()
				.log("Successfully initialized providers!");
	}

	@Override
	protected void setup() {
		LOGGER.atInfo()
				.log("Setting up plugin " + this.getName());

		File dataDir = this.getDataDirectory()
				.toFile();
		if (!dataDir.exists()) {
			LOGGER.atInfo()
					.log("Creating data directory");
			if (dataDir.mkdirs()) {
				LOGGER.atInfo()
						.log("Successfully created data directory!");
			} else {
				throw new RuntimeException("Failed to create data directory!");
			}
		}

		initializeProviders();
		registerEvents();
		if (configProvider.getFromSchema(ConfigSchema.DO_UPDATE_CHECKS)
				.getAsBoolean() && configProvider.getFromSchema(ConfigSchema.UPDATE_CHECK_FREQUENCY)
				.getAsInteger() != 0) {
			scheduleVersionChecker();
		}
	}
}