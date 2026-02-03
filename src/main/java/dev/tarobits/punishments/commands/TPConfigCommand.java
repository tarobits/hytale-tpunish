package dev.tarobits.punishments.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.gui.ManagementGui;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.VersionUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class TPConfigCommand extends CommandBase {
	public TPConfigCommand() {
		super("tpconfig", "tarobits.punishments.command.tpconfig");
		this.requirePermission(Permissions.CONFIG.getPermission());
		this.setUnavailableInSingleplayer(true);
		this.addSubCommand(new TPConfigReloadCommand());
		this.addSubCommand(new TPConfigVersionCommand());
		this.addSubCommand(new TPConfigCheckVersionCommand());
		this.addAliases("tpc");
	}

	@Override
	protected void executeSync(@NonNullDecl CommandContext ctx) {
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

	public static class TPConfigReloadCommand extends CommandBase {
		public TPConfigReloadCommand() {
			super("reload", "tarobits.punishments.command.tpconfig.reload");
			this.requirePermission(Permissions.CONFIG.getPermission());
			this.setUnavailableInSingleplayer(true);
		}

		@Override
		protected void executeSync(@Nonnull CommandContext ctx) {
			ConfigProvider.get()
					.loadConfig();
			ctx.sendMessage(Message.translation("tarobits.punishments.config.success.reload"));
		}
	}

	public static class TPConfigVersionCommand extends CommandBase {
		public TPConfigVersionCommand() {
			super("version", "tarobits.punishments.command.tpconfig.version");
			this.requirePermission(Permissions.CONFIG.getPermission());
			this.setUnavailableInSingleplayer(true);
		}

		@Override
		protected void executeSync(@NonNullDecl CommandContext ctx) {
			ctx.sendMessage(Message.translation("tarobits.punishments.version")
					                .param(
							                "version", TPunish.get()
									                .getVersion()
									                .toString()
					                ));
		}
	}

	public static class TPConfigCheckVersionCommand extends CommandBase {
		public TPConfigCheckVersionCommand() {
			super("checkversion", "tarobits.punishments.command.tpconfig.checkversion");
			this.requirePermission(Permissions.CONFIG.getPermission());
			this.setUnavailableInSingleplayer(true);
		}

		@Override
		protected void executeSync(@NonNullDecl CommandContext ctx) {
			Message msg = VersionUtils.checkVersions(TPunish.get()
					                                         .getVersion());
			if (msg == null) {
				ctx.sendMessage(Message.translation("tarobits.punishments.no_update"));
				return;
			}
			ctx.sendMessage(msg);
		}
	}
}
