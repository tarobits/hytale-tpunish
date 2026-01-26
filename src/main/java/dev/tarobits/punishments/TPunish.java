package dev.tarobits.punishments;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import dev.tarobits.punishments.commands.*;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.Version;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class TPunish extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static TPunish INSTANCE;
    private final PunishmentProvider punishmentProvider;
    private Boolean isOutdated;
    private Version newestVersion;
    private Version currentVersion;

    private final Config<TPunishConfig> config;

    private Boolean commandsRegistered = false;

    public TPunish(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        this.config = this.withConfig("config", TPunishConfig.CODEC);
        punishmentProvider = new PunishmentProvider();
        LOGGER.atInfo().log("TPunish (Version " + this.getManifest().getVersion().toString() + ")");
    }

    public static TPunish getInstance() {
        return INSTANCE;
    }

    public Config<TPunishConfig> getConfig() {
        return this.config;
    }

    private CompletableFuture<PlayerChatEvent> handleChat(CompletableFuture<PlayerChatEvent> future) {
        return future.thenApply(event -> {
            PlayerRef playerRef = event.getSender();
            UUID uuid = playerRef.getUuid();
            if (punishmentProvider.hasMute(uuid)) {
                playerRef.sendMessage(punishmentProvider.getActive(uuid, PunishmentType.MUTE).getReasonMessage());
                event.setCancelled(true);
            }
            return event;
        });
    }

    protected void registerCommands() {
        LOGGER.atInfo().log("Registering override commands.");
        this.getCommandRegistry().registerCommand(new BanCommand());
        this.getCommandRegistry().registerCommand(new UnbanCommand());
        this.getCommandRegistry().registerCommand(new MuteCommand());
        this.getCommandRegistry().registerCommand(new UnmuteCommand());
        this.getCommandRegistry().registerCommand(new WarnCommand());
        this.getCommandRegistry().registerCommand(new PunishCommand());
        this.getCommandRegistry().registerCommand(new KickCommand());
        this.commandsRegistered = true;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        File dataDir = this.getDataDirectory().toFile();
        if (!dataDir.exists()) {
            LOGGER.atInfo().log("Creating data directory");
            if (dataDir.mkdirs()) {
                LOGGER.atInfo().log("Successfully created data directory!");
            } else {
                throw new RuntimeException("Failed to create data directory!");
            }
        }
        this.config.load();
        this.config.save();
        this.currentVersion = Version.fromVersionString(this.getManifest().getVersion().toString());
        if (this.config.get().getShowUpdateNotifications()) {
            try {
                this.newestVersion = Version.getNewestVersion();
                this.isOutdated = this.currentVersion.isNewer(newestVersion);
            } catch (IllegalArgumentException _) {
                this.newestVersion = this.currentVersion;
                this.isOutdated = false;
                LOGGER.atWarning().log("Failed to get version!");
            }
        } else {
            this.newestVersion = this.currentVersion;
            this.isOutdated = false;
        }
        this.getEventRegistry().register(BootEvent.class, (_) -> {
            if (!this.commandsRegistered) {
                this.registerCommands();
            }
        });
        this.getEventRegistry().register(PlayerSetupConnectEvent.class, (event) -> {
            if (punishmentProvider.hasBan(event.getUuid())) {
                event.setReason(punishmentProvider.getActive(event.getUuid(), PunishmentType.BAN).getReasonMessage().getAnsiMessage());
                event.setCancelled(true);
            }
        });
        this.getEventRegistry().register(PlayerConnectEvent.class, (event) -> {
            if (Permissions.playerHas(event.getPlayerRef().getUuid(), Permissions.CONFIG) && this.isOutdated) {
                event.getPlayerRef().sendMessage(this.newestVersion.getUpdateMessage(this.currentVersion));
            }
        });
        this.getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, this::handleChat);
    }
}