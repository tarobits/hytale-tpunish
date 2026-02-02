package dev.tarobits.punishments.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.gui.ManagementGui;
import dev.tarobits.punishments.gui.PunishmentsGui;
import dev.tarobits.punishments.utils.Permissions;

import javax.annotation.Nonnull;

public class PunishCommand extends CommandBase {
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;

    public PunishCommand() {
        super("punish", "Open the punishment GUI");

        this.playerArg = this.withRequiredArg("player", "Player that is being punished", ArgTypes.GAME_PROFILE_LOOKUP);
        this.requirePermission(Permissions.OPEN_GUI.getPermission());
        this.setUnavailableInSingleplayer(true);
        this.addSubCommand(new PunishConfigCommand());
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            ctx.sendMessage(Message.raw("Cannot be executed from server console!"));
            return;
        }
        ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(ctx);
        if (profile == null) {
            ctx.sendMessage(Message.raw("Failed to get user profile!"));
            LOGGER.atWarning()
                    .log("Failed to get user profile!");
            return;
        }
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null) {
            ctx.sendMessage(Message.raw("Failed to get player store!"));
            return;
        }
        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = Universe.get()
                .getPlayer(ctx.sender()
                                   .getUuid());
        Player player = ctx.senderAs(Player.class);
        if (playerRef == null) {
            ctx.sendMessage(Message.raw("Failed to get player!"));
            return;
        }
        player.getPageManager()
                .openCustomPage(ref, store, new PunishmentsGui(playerRef, CustomPageLifetime.CanDismiss, profile));
    }

    public static class PunishConfigCommand extends CommandBase {
        public PunishConfigCommand() {
            super("config", "Edit TPunish config");
            this.requirePermission(Permissions.CONFIG.getPermission());
            this.setUnavailableInSingleplayer(true);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext ctx) {
            if (!ctx.isPlayer()) {
                ctx.sendMessage(Message.raw("Cannot be executed from server console!"));
                return;
            }

            Ref<EntityStore> ref = ctx.senderAsPlayerRef();
            if (ref == null) {
                ctx.sendMessage(Message.raw("Failed to get player store!"));
                return;
            }
            Store<EntityStore> store = ref.getStore();
            PlayerRef playerRef = Universe.get()
                    .getPlayer(ctx.sender()
                                       .getUuid());
            Player player = ctx.senderAs(Player.class);
            if (playerRef == null) {
                ctx.sendMessage(Message.raw("Failed to get player!"));
                return;
            }
            player.getPageManager()
                    .openCustomPage(ref, store, new ManagementGui(playerRef, CustomPageLifetime.CanDismiss));
        }
    }
}
