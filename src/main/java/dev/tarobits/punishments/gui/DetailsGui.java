package dev.tarobits.punishments.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.provider.AbstractProvider;
import dev.tarobits.punishments.provider.LogProvider;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.StringUtils;
import dev.tarobits.punishments.utils.TimeUtils;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.domainobject.Owner;
import dev.tarobits.punishments.utils.log.LogEntry;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.ui.HeaderBuilder;
import dev.tarobits.punishments.utils.ui.UIText;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.List;

public class DetailsGui extends InteractiveCustomUIPage<DetailsGui.DetailsData> {
	@Nullable
	private final ProfileServiceClient.PublicGameProfile target;
	private final Owner object;
	private final AbstractProvider<?> provider;

	public DetailsGui(
			PlayerRef playerRef,
			CustomPageLifetime lifetime,
			@Nullable ProfileServiceClient.PublicGameProfile target,
			Owner object
	) {
		super(playerRef, lifetime, DetailsData.CODEC);
		this.target = target;
		this.object = object;
		this.provider = object.type()
				.getProvider();
	}

	@Override
	public void handleDataEvent(
			@NonNullDecl Ref<EntityStore> ref,
			@NonNullDecl Store<EntityStore> store,
			@NonNullDecl DetailsData data
	) {
		super.handleDataEvent(ref, store, data);
		Player player = store.getComponent(ref, Player.getComponentType());
		PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
		DomainObject<?> item = provider.getFromId(object.id());
		if (player == null || playerRef == null) {
			throw new DeveloperErrorException("Player not found!");
		}
		UICommandBuilder uiCommandBuilder = new UICommandBuilder();
		UIEventBuilder uiEventBuilder = new UIEventBuilder();

		switch (data.action) {
			case "BackButton":
				if (target != null) {
					player.getPageManager()
							.openCustomPage(
									ref, store,
									new PunishmentsGui(playerRef, CustomPageLifetime.CanDismiss, target)
							);
				} else {
					player.getPageManager()
							.openCustomPage(ref, store, new ManagementGui(playerRef, CustomPageLifetime.CanDismiss));
				}
				return;
			case "Button1":
				if (item instanceof Punishment punishment) {
					try {
						if (punishment.canReinstate()) {
							// ToDo: Add reinstation
							throw new DeveloperErrorException("Not implemented yet!");
						} else if (punishment.canPardon()) {
							PunishmentProvider.get()
									.getFromId(punishment.getId())
									.pardon(playerRef.getUuid());
							this.build(ref, uiCommandBuilder, uiEventBuilder, store);
							this.sendUpdate(uiCommandBuilder, uiEventBuilder, true);
							return;
						}
					} catch (InvalidActionException | DeveloperErrorException e) {
						GuiUtil.sendToErrorPage(player, playerRef, ref, store, target, e.getMessage());
						return;
					}
				}
			case "Button2":
				throw new DeveloperErrorException("Not implemented yet!");
			case "Button3":
				throw new DeveloperErrorException("Not implemented yet!");
			case "Button4":
				throw new DeveloperErrorException("Not implemented yet!");
		}
		this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
	}

	@Override
	public void build(
			@NonNullDecl Ref<EntityStore> ref,
			@NonNullDecl UICommandBuilder uiCommandBuilder,
			@NonNullDecl UIEventBuilder uiEventBuilder,
			@NonNullDecl Store<EntityStore> store
	) {
		uiCommandBuilder.append("Pages/Tarobits_Punishments_DetailGui.ui");

		DomainObject<?> item = provider.getFromId(object.id());
		if (item == null) {
			throw new DeveloperErrorException("Object not found!");
		}

		String domainObjectString = StringUtils.toTitleCase(item.getDomainObjectType()
				                                                    .name());

		uiCommandBuilder.set("#HeaderTitle.Text", domainObjectString + " " + item.getId());
		uiCommandBuilder.set("#PageTitle.Text", domainObjectString + " Details");

		HeaderBuilder.buildHeader(item.getHeader(), uiCommandBuilder);

		List<LogEntry> logEntries = LogProvider.get()
				.getAllTargetIs(item.getDomainObjectType(), item.getId());

		if (item instanceof Punishment punishment) {
			uiCommandBuilder.set(
					"#Button1.Text", Message.translation(
							punishment.canReinstate() ? "tarobits.punishments.gui.details.buttons.reinstate" : "tarobits.punishments.gui.details.buttons.pardon")
			);
			uiCommandBuilder.set(
					"#Button2.Text", Message.translation("tarobits.punishments.gui.details.buttons.extend"));
			uiCommandBuilder.set(
					"#Button3.Text", Message.translation("tarobits.punishments.gui.details.buttons.reduce"));
			uiCommandBuilder.set(
					"#Button4.Text", Message.translation("tarobits.punishments.gui.details.buttons.viewappeal"));

			uiCommandBuilder.set("#Button1.Disabled", !(punishment.canReinstate() || punishment.canPardon()));
			uiCommandBuilder.set("#Button2.Disabled", !punishment.canExtend());
			uiCommandBuilder.set("#Button3.Disabled", !punishment.canReduce());
			uiCommandBuilder.set("#Button4.Visible", false);
			// ToDo: Enable if appeal exists

			linkEvents(uiEventBuilder);
		}

		buildList(uiCommandBuilder, uiEventBuilder, logEntries, item);
	}

	private void buildList(
			@NonNullDecl UICommandBuilder uiCommandBuilder,
			@NonNullDecl UIEventBuilder uiEventBuilder,
			@NonNullDecl List<LogEntry> logEntries,
			@NonNullDecl DomainObject<?> item
	) {
		String boxSelector = "#LogBox";
		for (int i = 0; i < logEntries.size(); i++) {
			LogEntry logEntry = logEntries.get(i);
			uiCommandBuilder.append(boxSelector, "Tarobits_Punishments_LogEntry.ui");
			String elementSelector = boxSelector + "[" + (i * 2) + "]";
			UIText logActionUIText = logEntry.getTargetLogActionUIText();
			uiCommandBuilder.set(elementSelector + " #Action.Text", logActionUIText.text());
			uiCommandBuilder.set(elementSelector + " #Action.Background", logActionUIText.style());
			uiCommandBuilder.set(
					elementSelector + " #Timestamp.Text", TimeUtils.instantAsDateTime(logEntry.getTimestamp()));
			uiCommandBuilder.set(elementSelector + " #ActionText.Text", logEntry.getTargetLogActionText());
			if (logEntries.size() - 1 > i) {
				uiCommandBuilder.append(boxSelector, "Tarobits_Punishments_LogEntrySpacer.ui");
			}
		}
	}

	protected void linkEvents(
			@NonNullDecl UIEventBuilder uiEventBuilder
	) {
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#BackButton", EventData.of(DetailsData.ACTION_KEY, "BackButton"));
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#Button1", EventData.of(DetailsData.ACTION_KEY, "Button1"));
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#Button2", EventData.of(DetailsData.ACTION_KEY, "Button2"));
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#Button3", EventData.of(DetailsData.ACTION_KEY, "Button3"));
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#Button4", EventData.of(DetailsData.ACTION_KEY, "Button4"));
	}

	public static class DetailsData {
		public static final String ACTION_KEY = "Action";
		public static final BuilderCodec<DetailsData> CODEC = BuilderCodec.builder(DetailsData.class, DetailsData::new)
				.append(new KeyedCodec<>(ACTION_KEY, Codec.STRING), (d, v) -> d.action = v, (d) -> d.action)
				.add()
				.build();
		private String action;
	}
}
