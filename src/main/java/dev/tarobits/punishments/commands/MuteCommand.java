package dev.tarobits.punishments.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.tarobits.punishments.exceptions.NoPermissionException;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.args.ArgUtils;
import dev.tarobits.punishments.utils.args.CustomArgumentType;
import dev.tarobits.punishments.utils.args.DurationArgType;
import dev.tarobits.punishments.utils.args.ReasonArgType;
import dev.tarobits.punishments.utils.punishment.Punishment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MuteCommand extends CommandBase {
    protected static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;
    private final RequiredArg<TimeFormat> timeArg;
    public MuteCommand() {
        super("mute", "Mute players.");
        this.setUnavailableInSingleplayer(true);
        this.requirePermission(Permissions.MUTE_COMMAND.getPermission());
        this.setAllowsExtraArguments(true);
        this.playerArg = this.withRequiredArg("player", "server.commands.ban.player.desc", ArgTypes.GAME_PROFILE_LOOKUP);
        this.timeArg = this.withRequiredArg("duration", "Duration for which the player should be banned.", new DurationArgType());
        this.withRequiredArg("reason", "Reason why the player is being banned.", new ReasonArgType());
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(ctx);
        TimeFormat duration = this.timeArg.get(ctx);
        Map<CustomArgumentType, String> args = ArgUtils.decodeArguments(ctx.getInputString(), 3, List.of(CustomArgumentType.DEFAULT));
        String reason = args.get(CustomArgumentType.DEFAULT);
        if (profile == null) {
            ctx.sendMessage(Message.raw("Failed to get user profile!"));
            LOGGER.atWarning().log("Failed to get user profile!");
            return;
        }
        UUID uuid = profile.getUuid();
        Message userName = Message.raw(profile.getUsername()).bold(true);
        if (punishmentProvider.hasMute(uuid)) {
            ctx.sendMessage(Message.translation("tarobits.punishments.mute.error.already").param("name", userName));
            return;
        }
        try {
            Punishment created = Punishment.createMute(uuid, ctx.sender().getUuid(), reason, duration);
            punishmentProvider.addPunishment(created);
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            ctx.sendMessage(created.getSuccessMessage(userName));
            if (playerRef == null) {
                return;
            }
            playerRef.sendMessage(created.getReasonMessage());
        } catch (NoPermissionException e) {
            ctx.sendMessage(Message.translation(e.getMessage()));
        }
    }
}
