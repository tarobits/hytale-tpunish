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
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nonnull;
import java.util.UUID;

public class UnWarnCommand extends CommandBase {
    private static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;
    private final RequiredArg<Integer> idArg;

    public UnWarnCommand() {
        super("remwarn", "Remove a warning from a player");
        this.requirePermission(Permissions.UNWARN.getPermission());
        this.playerArg = this.withRequiredArg("player", "Player to remove warning from", ArgTypes.GAME_PROFILE_LOOKUP);
        this.idArg = this.withRequiredArg("warn_id", "The ID of the warning you want to remove", ArgTypes.INTEGER);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(ctx);
        Integer warnId = this.idArg.get(ctx);
        if (profile == null) {
            ctx.sendMessage(Message.raw("Failed to get user profile!"));
            LOGGER.atWarning().log("Failed to get user profile!");
            return;
        }
        UUID uuid = profile.getUuid();
        Message userName = Message.raw(profile.getUsername()).bold(true);
        if (!punishmentProvider.editPunishment(uuid, PunishmentType.WARN, warnId, (Punishment d) -> {
            d.pardon();
            return true;
        })) {
            ctx.sendMessage(Message.translation("tarobits.punishments.warn.error.nowarn").param("name", userName).param("id", warnId));
            return;
        }
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        ctx.sendMessage(Message.translation("tarobits.punishments.warn.success.unwarn").param("name", userName).param("id", warnId));
        if (playerRef == null) { return; }
        playerRef.sendMessage(Message.translation("tarobits.punishments.warn.unwarn"));
    }
}
