package dev.tarobits.punishments.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.model.punishment.PunishmentType;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;

import javax.annotation.Nonnull;
import java.util.UUID;

public class UnbanCommand extends CommandBase {
    private static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;

    public UnbanCommand() {
	    super("unban", "tarobits.punishments.command.unban");
        this.requirePermission(Permissions.UNBAN.getPermission());
        this.setUnavailableInSingleplayer(true);
	    this.playerArg = this.withRequiredArg(
			    "tarobits.punishments.command.unban.args.player.display",
			    "tarobits.punishments.command.unban.args.player.desc", ArgTypes.GAME_PROFILE_LOOKUP
	    );
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
        if (!punishmentProvider.hasBan(uuid)) {
            ctx.sendMessage(Message.translation("tarobits.punishments.ban.error.isnt").param("name", userName));
            return;
        }
	    try {
		    ctx.sendMessage(punishmentProvider.getActive(uuid, PunishmentType.BAN)
				                    .pardon(ctx.sender()
						                            .getUuid())
				                    .param("action", "tarobits.punishments.edit.actions.unban")
				                    .param("player", userName));
	    } catch (InvalidActionException e) {
		    ctx.sendMessage(e.getChatMessage());
	    }
    }
}
