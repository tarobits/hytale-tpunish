package dev.tarobits.punishments.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.InvalidActionException;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.TimeFormat;
import dev.tarobits.punishments.utils.config.ConfigSchema;
import dev.tarobits.punishments.utils.config.PresetConfig;
import dev.tarobits.punishments.utils.punishment.PunishmentSubtype;
import dev.tarobits.punishments.utils.punishment.PunishmentType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditPresetsGui extends InteractiveCustomUIPage<GuiUtil.ListPunishmentsData> {
	private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
	private static ConfigProvider configProvider;
	private final List<PresetConfig> presetConfigs;
	private final Map<PunishmentType, Integer> tabButtonMap = new Object2ObjectOpenHashMap<>();
	private PunishmentType selectedTab;
	private PresetConfig selectedPunishment = null;
	private Integer selectedPunishmentIndex = -1;
	private GuiUtil.TypesSelected typesSelected = new GuiUtil.TypesSelected(
			PunishmentType.BAN,
	                                                                        PunishmentSubtype.PERMANENT
	);

	@SuppressWarnings("unchecked")
	public EditPresetsGui(
			@Nonnull PlayerRef playerRef,
			@Nonnull CustomPageLifetime lifetime
	) {
		super(playerRef, lifetime, GuiUtil.ListPunishmentsData.CODEC);
		configProvider = ConfigProvider.get();
		this.presetConfigs = new ArrayList<>((List<PresetConfig>) configProvider.getFromSchema(ConfigSchema.PRESETS)
				.getValue());
	}

	protected void resetHeader(
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull UIEventBuilder uiEventBuilder
	) {
		this.selectedPunishment = null;
		GuiUtil.buildPunishmentHeader(uiCommandBuilder, uiEventBuilder, "#Header", null, this.typesSelected, true);
	}

	@Override
	public void handleDataEvent(
			@NonNullDecl Ref<EntityStore> ref,
			@NonNullDecl Store<EntityStore> store,
			@NonNullDecl GuiUtil.ListPunishmentsData data
	) {
		super.handleDataEvent(ref, store, data);
		Player player = store.getComponent(ref, Player.getComponentType());
		PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
		if (player == null || playerRef == null) {
			throw new DeveloperErrorException("Player not found!");
		}
		UICommandBuilder uiCommandBuilder = new UICommandBuilder();
		UIEventBuilder uiEventBuilder = new UIEventBuilder();
		switch (data.action) {
			case "BackButton":
				player.getPageManager()
						.openCustomPage(ref, store, new ManagementGui(playerRef, CustomPageLifetime.CanDismiss));
				return;
			case "TypeDropdown":
				this.typesSelected = new GuiUtil.TypesSelected(data.typeDrop, data.subtype);
				GuiUtil.buildPunishmentHeader(
						uiCommandBuilder, uiEventBuilder, "#Header", null, this.typesSelected,
						false
				);
				break;
			case "TabButton":
				this.selectedTab = data.type;
				GuiUtil.buildTab(
						GuiUtil.ListTypes.EDIT, this.tabButtonMap, this.selectedTab, this.presetConfigs, ref,
						uiCommandBuilder, uiEventBuilder, store, null
				);
				break;
			case "Edit":
				this.selectedPunishment = presetConfigs.get(data.punishmentId);
				this.selectedPunishmentIndex = data.punishmentId;
				this.typesSelected = new GuiUtil.TypesSelected(
						this.selectedPunishment.getType(),
				                                               this.selectedPunishment.getSubType()
				);
				GuiUtil.buildPunishmentHeader(
						uiCommandBuilder, uiEventBuilder, "#Header", this.selectedPunishment,
						this.typesSelected, false
				);
				break;
			case "Submit":
				try {
					TimeFormat tf = TimeFormat.fromDurationString(data.duration);
					switch (this.typesSelected.type) {
						case WARN, KICK -> this.typesSelected = new GuiUtil.TypesSelected(
								this.typesSelected.type,
						                                                                  PunishmentSubtype.NULL
						);
					}
					if (this.selectedPunishment != null) {
						this.presetConfigs.set(
								this.selectedPunishmentIndex,
								new PresetConfig(
										data.name, this.typesSelected.type, this.typesSelected.subtype,
										tf, data.reason
								)
						);
					} else {
						this.presetConfigs.add(
								new PresetConfig(data.name, data.typeDrop, data.subtype, tf, data.reason));
					}
					configProvider.updateEntry(ConfigSchema.PRESETS, this.presetConfigs);
					this.selectedTab = this.typesSelected.type;
					resetHeader(uiCommandBuilder, uiEventBuilder);
					GuiUtil.buildTab(
							GuiUtil.ListTypes.EDIT, this.tabButtonMap, this.typesSelected.type,
							this.presetConfigs, ref, uiCommandBuilder, uiEventBuilder, store, null
					);
				} catch (IllegalArgumentException | InvalidActionException e) {
					GuiUtil.sendToErrorPage(player, playerRef, ref, store, null, e.getMessage());
					return;
				}
				break;
			case "Delete":
				try {
					this.presetConfigs.remove(data.punishmentId);
					configProvider.updateEntry(ConfigSchema.PRESETS, this.presetConfigs);
					GuiUtil.buildTab(
							GuiUtil.ListTypes.EDIT, this.tabButtonMap, this.selectedTab, this.presetConfigs, ref,
							uiCommandBuilder, uiEventBuilder, store, null
					);
				} catch (IllegalArgumentException | InvalidActionException e) {
					GuiUtil.sendToErrorPage(player, playerRef, ref, store, null, e.getMessage());
					return;
				}
				break;
			case "Reset":
				resetHeader(uiCommandBuilder, uiEventBuilder);
				break;
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
		if (!Permissions.playerHas(playerRef.getUuid(), Permissions.CONFIG)) {
			playerRef.sendMessage(Message.translation("tarobits.punishments.error.noperm"));
			this.close();
			return;
		}
		uiCommandBuilder.append("Pages/Tarobits_Punishments_ListPunishmentsGui.ui");

		uiCommandBuilder.set("#HeaderTitle.Text", Message.translation("tarobits.punishments.gui.editpresets.title"));
		uiCommandBuilder.set("#PageTitle.Text", "New Punishment");

		GuiUtil.buildPunishmentHeader(uiCommandBuilder, uiEventBuilder, "#Header", null, this.typesSelected, true);
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#BackButton",
				EventData.of(GuiUtil.ListPunishmentsData.ACTION_KEY, "BackButton")
		);
		GuiUtil.buildTabs(
				GuiUtil.ListTypes.EDIT, this.tabButtonMap, this.selectedTab, this.presetConfigs, ref,
				uiCommandBuilder, uiEventBuilder, store, null
		);
	}
}
