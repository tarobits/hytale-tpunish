package dev.tarobits.punishments.gui;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.config.ConfigEntry;
import dev.tarobits.punishments.config.ConfigEntryType;
import dev.tarobits.punishments.config.ConfigSchema;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;
import dev.tarobits.punishments.exceptions.UserException;
import dev.tarobits.punishments.provider.ConfigProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class EditConfigGui extends InteractiveCustomUIPage<EditConfigGui.EditConfigData> {
	private static final ConfigProvider PROVIDER = ConfigProvider.get();

	public EditConfigGui(
			@Nonnull PlayerRef playerRef,
			@Nonnull CustomPageLifetime lifetime
	) {
		super(playerRef, lifetime, EditConfigData.CODEC);
	}

	@Override
	public void handleDataEvent(
			@NonNullDecl Ref<EntityStore> ref,
			@NonNullDecl Store<EntityStore> store,
			@NonNullDecl EditConfigData data
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
			case "Details":
				// ToDo: Add configEntry details page
				throw new DeveloperErrorException("Not implemented yet!");
			case "Edit":
				ConfigEntry toEdit = PROVIDER.getFromSchema(data.setting);
				if (toEdit.getType().hasSpecialEditor) {
					GuiUtil.sendToErrorPage(player, playerRef, ref, store, null, null);
					throw new DeveloperErrorException("Wrong action!");
				}
				if (PROVIDER.editEntry(
						data.setting, (ConfigEntry c) -> {
							try {
								if (c.getType() == ConfigEntryType.BOOLEAN) {
									c.setValue(data.boolValue);
								} else if (c.getType() == ConfigEntryType.INTEGER) {
									c.setValue(data.integerValue);
								} else {
									throw new DeveloperErrorException("Not implemented yet!");
								}
								return true;
							} catch (UserException e) {
								return false;
							}
						}
				)) {
					this.build(ref, uiCommandBuilder, uiEventBuilder, store);
					this.sendUpdate(uiCommandBuilder, uiEventBuilder, true);
				} else {
					GuiUtil.sendToErrorPage(player, playerRef, ref, store, null, null);
				}
				return;
			default:
				throw new DeveloperErrorException("Wrong action!");
		}
	}

	@Override
	public void build(
			@NonNullDecl Ref<EntityStore> ref,
			@NonNullDecl UICommandBuilder uiCommandBuilder,
			@NonNullDecl UIEventBuilder uiEventBuilder,
			@NonNullDecl Store<EntityStore> store
	) {
		uiCommandBuilder.append("Pages/Tarobits_Punishments_EditConfigGui.ui");

		uiEventBuilder.addEventBinding(
				CustomUIEventBindingType.Activating, "#BackButton",
				EventData.of(EditConfigData.ACTION_KEY, "BackButton")
		);

		String boxSelector = "#Entries";
		int i = 0;
		for (ConfigEntry entry : PROVIDER.getAllSorted()) {
			String selector = boxSelector + "[" + i + "]";
			uiCommandBuilder.append(boxSelector, "Tarobits_Punishments_ConfigEntry.ui");
			uiCommandBuilder.set(selector + " #Key.Text", entry.getKey());
			if (entry.getType().hasSpecialEditor) {
				uiCommandBuilder.set(selector + " #DefaultValue.Text", "Cannot be displayed");
				uiCommandBuilder.set(selector + " #CurrentValue.Text", "Cannot be displayed");
				uiCommandBuilder.appendInline(
						selector + " #EditBox",
						"Label { Style: (FontSize: 16, TextColor: #d0d0d0, HorizontalAlignment: Center, VerticalAlignment: Center); Text: \"This value cannot be edited here\";}"
				);
			} else {
				uiCommandBuilder.set(
						selector + " #DefaultValue.Text", entry.getDefaultValue()
								.toString()
				);
				uiCommandBuilder.set(selector + " #CurrentValue.Text", entry.displayValue());
				switch (entry.getType()) {
					case BOOLEAN -> {
						uiCommandBuilder.append(selector + " #EditBox", "Tarobits_Punishments_BoolVal.ui");
						uiCommandBuilder.set(selector + " #EditBox #ValueBox.Value", entry.getAsBoolean());
						uiEventBuilder.addEventBinding(
								CustomUIEventBindingType.Activating, selector + " #EditBox #SaveButton",
								EventData.of(EditConfigData.ACTION_KEY, "Edit")
										.append(
												EditConfigData.SETTING_KEY, entry.getConfigSchema()
														.name()
										)
										.append(
												EditConfigData.BOOLEAN_KEY, selector + " #EditBox #ValueBox.Value")
						);
					}
					case INTEGER, DECIMAL -> {
						uiCommandBuilder.append(selector + " #EditBox", "Tarobits_Punishments_NumberVal.ui");
						if (entry.getType() == ConfigEntryType.INTEGER) {
							uiCommandBuilder.set(selector + " #EditBox #ValueBox.Value", entry.getAsInteger());
							uiEventBuilder.addEventBinding(
									CustomUIEventBindingType.Activating, selector + " #EditBox #SaveButton",
									EventData.of(EditConfigData.ACTION_KEY, "Edit")
											.append(
													EditConfigData.SETTING_KEY, entry.getConfigSchema()
															.name()
											)
											.append(
													EditConfigData.INTEGER_KEY, selector + " #EditBox #ValueBox.Value")
							);
						} else {
							uiCommandBuilder.set(
									selector + " #EditBox #ValueBox.Value", entry.getAsBigDecimal()
											.toString()
							);
						}
					}
				}
			}
			i++;
		}
	}

	public static class EditConfigData {
		public static final String ACTION_KEY = "Action";
		public static final String SETTING_KEY = "Setting";
		public static final String BOOLEAN_KEY = "@Boolean";
		public static final String INTEGER_KEY = "@Integer";
		public static final BuilderCodec<EditConfigData> CODEC = BuilderCodec.builder(
						EditConfigData.class, EditConfigData::new)
				.append(new KeyedCodec<>(ACTION_KEY, BuilderCodec.STRING), (d, v) -> d.action = v, (d) -> d.action)
				.add()
				.append(
						new KeyedCodec<>(SETTING_KEY, BuilderCodec.STRING),
						(d, v) -> d.setting = ConfigSchema.valueOf(v), (d) -> d.setting.name()
				)
				.add()
				.append(
						new KeyedCodec<>(BOOLEAN_KEY, BuilderCodec.BOOLEAN), (d, v) -> d.boolValue = v,
						(d) -> d.boolValue
				)
				.add()
				.append(
						new KeyedCodec<>(INTEGER_KEY, BuilderCodec.INTEGER), (d, v) -> d.integerValue = v,
						(d) -> d.integerValue
				)
				.add()
				.build();
		private String action;
		private ConfigSchema setting;
		private Boolean boolValue;
		private Integer integerValue;
	}
}
