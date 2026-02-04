package dev.tarobits.punishments.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.model.punishment.Punishment;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.args.ArgUtils;
import dev.tarobits.punishments.utils.args.CustomArgumentType;
import dev.tarobits.punishments.utils.args.ReasonArgType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KickCommand extends CommandBase {
	private static final PunishmentProvider punishmentProvider = PunishmentProvider.get();
	private final RequiredArg<PlayerRef> playerArg;

	public KickCommand() {
		super("kick", "tarobits.punishments.command.kick");
		this.setUnavailableInSingleplayer(true);
		this.setAllowsExtraArguments(true);
		this.requirePermission(Permissions.KICK_COMMAND.getPermission());
		this.playerArg = this.withRequiredArg(
				"tarobits.punishments.command.kick.args.player.display",
				"tarobits.punishments.command.kick.args.player.desc", ArgTypes.PLAYER_REF
		);
		this.withRequiredArg(
				"tarobits.punishments.command.kick.args.reason.display",
				"tarobits.punishments.command.kick.args.reason.desc", new ReasonArgType()
		);
	}

	@Override
	protected void executeSync(@Nonnull CommandContext ctx) {
		PlayerRef playerRef = this.playerArg.get(ctx);
		Map<CustomArgumentType, String> args = ArgUtils.decodeArguments(
				ctx.getInputString(), 3,
				List.of(CustomArgumentType.DEFAULT)
		);
		String reason = args.get(CustomArgumentType.DEFAULT);
		if (playerRef == null) {
			ctx.sendMessage(Message.translation("tarobits.punishments.kick.error.noplayer")
					                .param("reason", reason));
			return;
		}
		UUID uuid = playerRef.getUuid();
		Message userName = Message.raw(playerRef.getUsername())
				.bold(true);

		try {
			Punishment created = Punishment.createKick(
					uuid, ctx.sender()
							.getUuid(), reason
			);
			punishmentProvider.addEntry(created);
			playerRef.getPacketHandler()
					.disconnect(created.getReasonMessage()
							            .getAnsiMessage());
			ctx.sendMessage(Message.translation("tarobits.punishments.kick.success")
					                .param("name", userName)
					                .param("reason", reason));
		} catch (UserException e) {
			ctx.sendMessage(Message.translation(e.getMessage()));
		}
	}
}
