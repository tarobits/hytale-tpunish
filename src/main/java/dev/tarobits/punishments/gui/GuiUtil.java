package dev.tarobits.punishments.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.config.PresetConfig;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.model.punishment.Punishment;
import dev.tarobits.punishments.model.punishment.PunishmentSubtype;
import dev.tarobits.punishments.model.punishment.PunishmentType;
import dev.tarobits.punishments.ui.HistoryStat;
import dev.tarobits.punishments.ui.PunishmentEntryBuilder;
import dev.tarobits.punishments.ui.UIText;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.PlayerUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiUtil {

	protected static void buildPunishmentHeader(
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull UIEventBuilder uiEventBuilder,
			@Nonnull String headerSelector,
			@Nullable PresetConfig selectedPunishment,
			@Nonnull TypesSelected typesSelected,
			@Nonnull Boolean reset
	) {
		if (reset) {
			uiCommandBuilder.clear(headerSelector);
			uiCommandBuilder.append(headerSelector, "Tarobits_Punishments_PHeader.ui");
			uiCommandBuilder.set(
					headerSelector + " #TypeDropdown.Entries", Arrays.stream(PunishmentType.values())
							.map(type -> new DropdownEntryInfo(
									LocalizableString.fromString(type.toDisplayString()),
									type.toJson()
							))
							.collect(Collectors.toList())
			);
			uiCommandBuilder.set(
					headerSelector + " #SubtypeDropdown.Entries", Arrays.stream(PunishmentSubtype.values())
							.filter(p -> !p.equals(PunishmentSubtype.NULL))
							.map(type -> new DropdownEntryInfo(
									LocalizableString.fromString(type.toDisplayString()),
									type.toJson()
							))
							.collect(Collectors.toList())
			);
			uiCommandBuilder.set(headerSelector + " #SubmitButton.Text", "Add");
			uiCommandBuilder.set("#PageTitle.Text", "New Punishment");
		}
		boolean showSub = typesSelected.type == PunishmentType.MUTE || typesSelected.type == PunishmentType.BAN;
		uiCommandBuilder.set(headerSelector + " #SubtypeContainer.Visible", showSub);
		uiCommandBuilder.set(headerSelector + " #SubtypeSpacer.Visible", !showSub);
		uiCommandBuilder.set(
				headerSelector + " #DurationBox.Visible",
				showSub && typesSelected.subtype == PunishmentSubtype.TEMPORARY
		);
		uiCommandBuilder.set(headerSelector + " #TypeDropdown.Value", typesSelected.type.toJson());
		uiCommandBuilder.set(headerSelector + " #SubtypeDropdown.Value", typesSelected.subtype.toJson());

		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.ValueChanged, headerSelector + " #TypeDropdown",
				EventData.of(ListPunishmentsData.ACTION_KEY, "TypeDropdown")
						.append(ListPunishmentsData.TYPE_DROP_KEY, headerSelector + " #TypeDropdown.Value")
						.append(ListPunishmentsData.SUBTYPE_KEY, headerSelector + " #SubtypeDropdown.Value")
		);
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.ValueChanged, headerSelector + " #SubtypeDropdown",
				EventData.of(ListPunishmentsData.ACTION_KEY, "TypeDropdown")
						.append(ListPunishmentsData.TYPE_DROP_KEY, headerSelector + " #TypeDropdown.Value")
						.append(ListPunishmentsData.SUBTYPE_KEY, headerSelector + " #SubtypeDropdown.Value")
		);
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, headerSelector + " #SubmitButton",
				EventData.of(ListPunishmentsData.ACTION_KEY, "Submit")
						.append(ListPunishmentsData.TYPE_DROP_KEY, headerSelector + " #TypeDropdown.Value")
						.append(ListPunishmentsData.DURATION_KEY, headerSelector + " #Duration.Value")
						.append(ListPunishmentsData.NAME_KEY, headerSelector + " #Name.Value")
						.append(ListPunishmentsData.REASON_KEY, headerSelector + " #Reason.Value")
						.append(ListPunishmentsData.SUBTYPE_KEY, headerSelector + " #SubtypeDropdown.Value")
		);
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, headerSelector + " #ResetButton",
				EventData.of(ListPunishmentsData.ACTION_KEY, "Reset")
		);
		if (selectedPunishment == null) {
			return;
		}
		uiCommandBuilder.set(headerSelector + " #SubmitButton.Text", "Update");
		uiCommandBuilder.set("#PageTitle.Text", "Editing " + selectedPunishment.getName());
		uiCommandBuilder.set(headerSelector + " #Name.Value", selectedPunishment.getName());
		uiCommandBuilder.set(
				headerSelector + " #Duration.Value", selectedPunishment.getDuration()
						.toFullDurationString()
		);
		uiCommandBuilder.set(headerSelector + " #Reason.Value", selectedPunishment.getReason());
		uiCommandBuilder.set(
				headerSelector + " #TypeDropdown.Value", selectedPunishment.getType()
						.toJson()
		);
		if (selectedPunishment.getSubType() != PunishmentSubtype.NULL) {
			uiCommandBuilder.set(
					headerSelector + " #SubtypeDropdown.Value", selectedPunishment.getSubType()
							.toJson()
			);
			uiCommandBuilder.set(headerSelector + " #SubtypeContainer.Visible", true);
			uiCommandBuilder.set(headerSelector + " #SubtypeSpacer.Visible", false);
		} else {
			uiCommandBuilder.set(headerSelector + " #SubtypeContainer.Visible", false);
			uiCommandBuilder.set(headerSelector + " #SubtypeSpacer.Visible", true);
		}

	}

	protected static void buildPlayerHeader(
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull Map<PunishmentType, HistoryStat> historyStats,
			@Nonnull String headerSelector
	) {
		uiCommandBuilder.append(headerSelector, "Tarobits_Punishments_Header.ui");

		for (Map.Entry<PunishmentType, HistoryStat> entry : historyStats.entrySet()) {
			PunishmentType punishmentType = entry.getKey();
			String typeString = entry.getKey()
					.toDisplayString();
			UIText statusText = PunishmentEntryBuilder.buildStatusText(
					entry.getValue().latestPunishment, entry.getKey()
							.toDisplayString()
			);
			if (punishmentType == PunishmentType.MUTE || punishmentType == PunishmentType.BAN) {
				uiCommandBuilder.set("#" + typeString + "Status.Text", statusText.text());
				uiCommandBuilder.set("#" + typeString + "Status.Style", statusText.style());
			}
			uiCommandBuilder.set(
					"#" + typeString + "Stats" + (entry.getValue().numberOfPunishments == 0 ? "None" : "Some") + ".Visible",
					true
			);
			uiCommandBuilder.set(
					"#" + typeString + "Stats" + (entry.getValue().numberOfPunishments != 0 ? "None" : "Some") + ".Visible",
					false
			);
			uiCommandBuilder.set("#" + typeString + "StatsSome.Text", entry.getValue().numberOfPunishments.toString());
		}
	}

	protected static void buildPunishmentList(
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull UIEventBuilder uiEventBuilder,
			@Nonnull List<Punishment> punishments,
			@Nonnull String listSelector,
			@Nonnull String typeString
	) {
		if (punishments.isEmpty()) {
			uiCommandBuilder.append(listSelector, "Tarobits_Punishments_EmptyRow.ui");
			uiCommandBuilder.set(listSelector + " #EmptyText.Text", "No " + typeString.toLowerCase() + " history");
			return;
		}

		int i = 0;
		for (Punishment punishment : punishments) {
			if (punishment == null) {
				continue;
			}

			String selector = listSelector + "[" + i++ + "]";
			uiCommandBuilder.append(listSelector, "Tarobits_Punishments_Entry.ui");

			setPunishmentEvents(uiEventBuilder, selector, punishment.getId());

			setCommonFields(uiCommandBuilder, selector, punishment);

			setStatusFields(uiCommandBuilder, selector, punishment);
		}
	}

	private static void setPunishmentEvents(
			@Nonnull UIEventBuilder uiEventBuilder,
			@Nonnull String selector,
			@Nonnull UUID id
	) {
		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, selector + " #DetailsButton",
				EventData.of(PunishmentsGui.PunishGuiData.BUTTON_KEY, "DetailsButton")
						.append(PunishmentsGui.PunishGuiData.ID_KEY, id.toString())
		);
	}

	private static void setCommonFields(
			UICommandBuilder uiCommandBuilder,
			String selector,
			Punishment punishment
	) {
		uiCommandBuilder.set(selector + " #Type.Text", punishment.getDisplaySubType());
		uiCommandBuilder.set(selector + " #Date.Text", punishment.getDate());
		uiCommandBuilder.set(selector + " #By.Text", PlayerUtils.getUsername(punishment.getBy()));
		uiCommandBuilder.set(selector + " #Reason.Text", punishment.getReason());
		uiCommandBuilder.set(selector + " #Duration.Text", punishment.getRelativeDuration());
		uiCommandBuilder.set(selector + " #Until.Text", punishment.getUntil());
		uiCommandBuilder.set(selector + " #Type.Text", punishment.getDisplaySubType());

		uiCommandBuilder.set(selector + " #DurationBox.Visible", false);
		uiCommandBuilder.set(selector + " #UntilBox.Visible", punishment.canExpire());
		uiCommandBuilder.set(selector + " #TypeBox.Visible", punishment.getSubtype() != PunishmentSubtype.NULL);
	}

	private static void setStatusFields(
			UICommandBuilder uiCommandBuilder,
			String selector,
			Punishment punishment
	) {
		if (punishment.isActive() && !punishment.isPardoned()) {
			uiCommandBuilder.set(selector + " #StatusExpired.Visible", false);
			uiCommandBuilder.set(selector + " #StatusPardoned.Visible", false);
			uiCommandBuilder.set(selector + " #StatusActive.Visible", true);
		} else if (punishment.isPardoned()) {
			uiCommandBuilder.set(selector + " #StatusExpired.Visible", false);
			uiCommandBuilder.set(selector + " #StatusActive.Visible", false);
			uiCommandBuilder.set(selector + " #StatusPardoned.Visible", true);
		}
	}

	protected static void buildTabs(
			@Nonnull ListTypes type,
			@Nonnull Map<PunishmentType, Integer> tabButtonMap,
			@Nonnull PunishmentType selectedTab,
			@Nonnull List<PresetConfig> presetConfigs,
			@Nonnull Ref<EntityStore> ref,
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull UIEventBuilder uiEventBuilder,
			@Nonnull Store<EntityStore> store,
			@Nullable PlayerRef playerRef
	) {
		int i = 0;
		tabButtonMap.clear();
		for (PunishmentType p : PunishmentType.values()) {
			if (playerRef != null) {
				if (!Permissions.playerHasAny(playerRef.getUuid(), Permissions.getPermissionsFromType(p))) {
					continue;
				}
			}
			if (tabButtonMap.containsKey(p)) {
				continue;
			}
			String selector = "#TabButtons[" + i + "]";
			uiCommandBuilder.append("#TabButtons", "Tarobits_Punishments_TabButton.ui");
			uiCommandBuilder.set(selector + ".Text", p.toDisplayString());
			tabButtonMap.put(p, i);
			uiEventBuilder.addEventBinding(
					CustomUIEventBindingType.Activating, selector,
					EventData.of(ListPunishmentsData.ACTION_KEY, "TabButton")
							.append(ListPunishmentsData.TYPE_KEY, p)
			);
			i++;
		}
		selectedTab = PunishmentType.values()[0];
		buildTab(
				type, tabButtonMap, selectedTab, presetConfigs, ref, uiCommandBuilder, uiEventBuilder, store,
				playerRef
		);
	}

	protected static void buildTab(
			@Nonnull ListTypes type,
			@Nonnull Map<PunishmentType, Integer> tabButtonMap,
			@Nonnull PunishmentType selectedTab,
			@Nonnull List<PresetConfig> presetConfigs,
			@Nonnull Ref<EntityStore> ref,
			@Nonnull UICommandBuilder uiCommandBuilder,
			@Nonnull UIEventBuilder uiEventBuilder,
			@Nonnull Store<EntityStore> store,
			@Nullable PlayerRef playerRef
	) {
		tabButtonMap.forEach((t, i) -> {
			boolean value = t == selectedTab;
			String selector = "#TabButtons[" + i + "]";
			uiCommandBuilder.set(selector + ".Disabled", value);
		});

		uiCommandBuilder.clear("#Tab");
		int rowNum = 0;
		int inCurrentRow = 0;
		int totalNumber = 0;
		for (PresetConfig p : presetConfigs) {
			totalNumber++;
			if (playerRef != null) {
				if (!Permissions.playerHas(
						playerRef.getUuid(),
						Permissions.getPermissionFromTypes(p.getType(), p.getSubType())
				)) {
					continue;
				}
			}
			if (p.getType() != selectedTab) {
				continue;
			}
			if (inCurrentRow == 0) {
				uiCommandBuilder.appendInline(
						"#Tab",
						"Group { FlexWeight: 1; LayoutMode: Left; Padding: (Left: 5, Top: 5, Right: 5); }"
				);
			}
			String rowSelector = "#Tab[" + rowNum + "]";
			String itemSelector = rowSelector + "[" + inCurrentRow + "]";
			uiCommandBuilder.append(rowSelector, "Tarobits_Punishments_PunishmentButton.ui");
			uiCommandBuilder.set(itemSelector + " #Name.Text", p.getName());
			uiCommandBuilder.set(
					itemSelector + " #Type.Text", p.getSubType()
							.toDisplayString()
			);
			uiCommandBuilder.set(
					itemSelector + " #Duration.Text", p.getDuration()
							.toFullDurationString(false)
			);
			if (p.getSubType() != PunishmentSubtype.TEMPORARY) {
				uiCommandBuilder.set(itemSelector + " #Duration.Visible", false);
			}
			if (p.getSubType() == PunishmentSubtype.NULL) {
				uiCommandBuilder.set(itemSelector + " #Type.Visible", false);
			}
			if (type == ListTypes.EDIT) {
				uiCommandBuilder.set(itemSelector + " #EditButton.Visible", true);
				uiEventBuilder.addEventBinding(
						CustomUIEventBindingType.Activating, itemSelector + " #EditButton",
						EventData.of(ListPunishmentsData.ACTION_KEY, "Edit")
								.append(ListPunishmentsData.PUNISHMENT_KEY, String.valueOf(totalNumber - 1))
				);
				uiCommandBuilder.set(itemSelector + " #ExecuteButton.Visible", true);
				uiCommandBuilder.set(itemSelector + " #ExecuteButton.Text", "Delete");
				uiEventBuilder.addEventBinding(
						CustomUIEventBindingType.Activating, itemSelector + " #ExecuteButton",
						EventData.of(ListPunishmentsData.ACTION_KEY, "Delete")
								.append(ListPunishmentsData.PUNISHMENT_KEY, String.valueOf(totalNumber - 1))
				);
			} else if (type == ListTypes.ADD) {
				uiCommandBuilder.set(itemSelector + " #ExecuteButton.Visible", true);
				uiEventBuilder.addEventBinding(
						CustomUIEventBindingType.Activating, itemSelector + " #ExecuteButton",
						EventData.of(ListPunishmentsData.ACTION_KEY, "Execute")
								.append(ListPunishmentsData.PUNISHMENT_KEY, String.valueOf(totalNumber - 1))
				);
			}
			inCurrentRow++;
			if (inCurrentRow > 4) {
				inCurrentRow = 0;
				rowNum++;
			}
		}
	}

	public static void sendToErrorPage(
			@Nonnull Player player,
			@Nonnull PlayerRef playerRef,
			@Nonnull Ref<EntityStore> ref,
			@Nonnull Store<EntityStore> store,
			@Nullable ProfileServiceClient.PublicGameProfile target,
			@Nullable String errorMessageId
	) {
		player.getPageManager()
				.openCustomPage(
						ref, store, new ErrorGui(
								playerRef, CustomPageLifetime.CanDismiss, target,
								errorMessageId == null ? "tarobits.punishments.error.generic" : errorMessageId, player
						)
				);
	}

	protected enum ListTypes {
		ADD,
		EDIT
	}

	protected static class TypesSelected {
		protected PunishmentType type;
		protected PunishmentSubtype subtype;

		public TypesSelected(
				@Nonnull PunishmentType type,
				@Nonnull PunishmentSubtype subtype
		) {
			this.type = type;
			this.subtype = subtype;
		}
	}

	public static class ListPunishmentsData {
		public static final String ACTION_KEY = "Action";
		public static final String TYPE_KEY = "Type";
		public static final String PUNISHMENT_KEY = "Punishment";
		public static final String NAME_KEY = "@Name";
		public static final String DURATION_KEY = "@Duration";
		public static final String REASON_KEY = "@Reason";
		public static final String SUBTYPE_KEY = "@Subtype";
		public static final String TYPE_DROP_KEY = "@Type";
		public static final BuilderCodec<ListPunishmentsData> CODEC = BuilderCodec.builder(
						ListPunishmentsData.class,
						ListPunishmentsData::new
				)
				.append(new KeyedCodec<String>(ACTION_KEY, Codec.STRING), (d, v) -> d.action = v, (d) -> d.action)
				.add()
				.append(
						new KeyedCodec<String>(TYPE_KEY, Codec.STRING), (d, v) -> {
							try {
								d.type = PunishmentType.fromJson(v.toLowerCase());
							} catch (UserException e) {
								throw new DeveloperErrorException(e.getTextMessage());
							}
						}, (d) -> d.type.toJson()
				)
				.add()
				.append(
						new KeyedCodec<String>(PUNISHMENT_KEY, Codec.STRING),
						(d, v) -> d.punishmentId = Integer.parseInt(v), (d) -> String.valueOf(d.punishmentId)
				)
				.add()
				.append(new KeyedCodec<String>(NAME_KEY, Codec.STRING), (d, v) -> d.name = v, (d) -> d.name)
				.add()
				.append(new KeyedCodec<String>(DURATION_KEY, Codec.STRING), (d, v) -> d.duration = v, (d) -> d.duration)
				.add()
				.append(new KeyedCodec<String>(REASON_KEY, Codec.STRING), (d, v) -> d.reason = v, (d) -> d.reason)
				.add()
				.append(
						new KeyedCodec<String>(SUBTYPE_KEY, Codec.STRING), (d, v) -> {
							try {
								if (v == null) {
									d.subtype = PunishmentSubtype.NULL;
								} else {
									d.subtype = PunishmentSubtype.fromJson(v.toLowerCase());
								}
							} catch (UserException _) {
								d.subtype = PunishmentSubtype.NULL;
							}
						}, (d) -> d.subtype.toJson()
				)
				.add()
				.append(
						new KeyedCodec<String>(TYPE_DROP_KEY, Codec.STRING), (d, v) -> {
							try {
								d.typeDrop = PunishmentType.fromJson(v.toLowerCase());
							} catch (UserException e) {
								throw new DeveloperErrorException(e.getTextMessage());
							}
						}, (d) -> d.typeDrop.toJson()
				)
				.add()
				.build();
		protected String action;
		protected PunishmentType type;
		protected int punishmentId;
		protected String name;
		protected String duration;
		protected String reason;
		protected PunishmentSubtype subtype;
		protected PunishmentType typeDrop;
	}
}
