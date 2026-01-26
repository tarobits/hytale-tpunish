package dev.tarobits.punishments.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nonnull;
import java.util.UUID;

public class UnmuteCommand extends CommandBase {
    private static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;

    public UnmuteCommand() {
        super("unmute", "Unmute players");
        this.requirePermission(Permissions.UNMUTE.getPermission());
        this.setUnavailableInSingleplayer(true);
        this.playerArg = this.withRequiredArg("player", "Player to unmute", ArgTypes.GAME_PROFILE_LOOKUP);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(ctx);
        if (profile == null) {
            ctx.sendMessage(Message.raw("Failed to get user profile!"));
            LOGGER.atWarning().log("Failed to get user profile!");
            return;
        }
        UUID uuid = profile.getUuid();
        Message userName = Message.raw(profile.getUsername()).bold(true);
        if (!punishmentProvider.hasMute(uuid)) {
            ctx.sendMessage(Message.translation("tarobits.punishments.mute.error.isnt").param("name", userName));
            return;
        }
        ctx.sendMessage(punishmentProvider.getActive(uuid, PunishmentType.MUTE).pardon().param("action", "tarobits.punishments.edit.actions.unmute").param("player", userName));
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) { return; }
        playerRef.sendMessage(Message.translation("tarobits.punishments.mute.unmute"));
    }
}
