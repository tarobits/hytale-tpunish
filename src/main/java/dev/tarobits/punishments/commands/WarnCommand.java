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
import dev.tarobits.punishments.utils.args.ArgUtils;
import dev.tarobits.punishments.utils.args.CustomArgumentType;
import dev.tarobits.punishments.utils.args.ReasonArgType;
import dev.tarobits.punishments.utils.punishment.Punishment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarnCommand extends CommandBase {
    private static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
    private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg;

    public WarnCommand() {
        super("warn", "Warn players");
        this.setUnavailableInSingleplayer(true);
        this.requirePermission(Permissions.WARN_COMMAND.getPermission());
        this.setAllowsExtraArguments(true);
        this.playerArg = this.withRequiredArg("player", "Player to warn", ArgTypes.GAME_PROFILE_LOOKUP);
        this.withRequiredArg("reason", "The reason to warn this player", new ReasonArgType());
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(ctx);
        Map<CustomArgumentType, String> args = ArgUtils.decodeArguments(ctx.getInputString(), 3, List.of(CustomArgumentType.DEFAULT));
        String reason = args.get(CustomArgumentType.DEFAULT);
        if (profile == null) {
            ctx.sendMessage(Message.raw("Failed to get user profile!"));
            LOGGER.atWarning().log("Failed to get user profile!");
            return;
        }
        UUID uuid = profile.getUuid();
        Message userName = Message.raw(profile.getUsername()).bold(true);
        try {
            Punishment created = Punishment.createWarn(uuid, ctx.sender().getUuid(), reason);
            punishmentProvider.addEntry(created);
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            ctx.sendMessage(Message.translation("tarobits.punishments.warn.success.warn").param("name", userName).param("reason", reason));
            if (playerRef == null) {
                return;
            }
            playerRef.sendMessage(created.getReasonMessage());
        } catch (NoPermissionException e) {
            ctx.sendMessage(Message.translation(e.getMessage()));
        }
    }
}
