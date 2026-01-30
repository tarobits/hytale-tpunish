package dev.tarobits.punishments.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.NoPermissionException;
import dev.tarobits.punishments.provider.ConfigProvider;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.config.ConfigSchema;
import dev.tarobits.punishments.utils.config.PresetConfig;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentType;
import dev.tarobits.punishments.utils.ui.HistoryStat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.tarobits.punishments.gui.GuiUtil.buildTab;
import static dev.tarobits.punishments.gui.GuiUtil.buildTabs;

public class AddPunishmentGui extends InteractiveCustomUIPage<GuiUtil.ListPunishmentsData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final PunishmentProvider pp = PunishmentProvider.get();
    private final ProfileServiceClient.PublicGameProfile target;
    private final List<PresetConfig> presetConfigs;
    private final Map<PunishmentType, Integer> tabButtonMap = new Object2ObjectOpenHashMap<>();
    private final Map<PunishmentType, HistoryStat> historyStats;
    private PunishmentType selectedTab;

    @SuppressWarnings("unchecked")
    public AddPunishmentGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, @Nonnull ProfileServiceClient.PublicGameProfile target, @Nonnull Map<PunishmentType, HistoryStat> historyStats) {
        super(playerRef, lifetime, GuiUtil.ListPunishmentsData.CODEC);
        this.target = target;
        this.presetConfigs = new ArrayList<>((List<PresetConfig>) ConfigProvider.get().getFromSchema(ConfigSchema.PRESETS).getValue());
        this.historyStats = historyStats;
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull GuiUtil.ListPunishmentsData data) {
        super.handleDataEvent(ref, store, data);
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
            throw new IllegalArgumentException("Player not found!");
        }
        switch (data.action) {
            case "BackButton":
                player.getPageManager().openCustomPage(ref, store, new PunishmentsGui(playerRef, CustomPageLifetime.CanDismiss, target));
                return;
            case "TabButton":
                this.selectedTab = data.type;
                break;
            case "Execute":
                try {
                    PresetConfig pc = presetConfigs.get(data.punishmentId);
                    Punishment created = null;
                    String hasPunishment = pp.userHasPunishment(target.getUuid(), pc.getType());
                    if (hasPunishment != null) {
                        GuiUtil.sendToErrorPage(player, playerRef, ref, store, target, "tarobits.punishments." + hasPunishment + ".error.already");
                        return;
                    }
                    switch (pc.getType()) {
                        case BAN ->
                                pp.addEntry(created = Punishment.createBan(target.getUuid(), playerRef.getUuid(), pc.getReason(), pc.getDuration()));
                        case MUTE ->
                                pp.addEntry(created = Punishment.createMute(target.getUuid(), playerRef.getUuid(), pc.getReason(), pc.getDuration()));
                        case KICK ->
                                pp.addEntry(created = Punishment.createKick(target.getUuid(), playerRef.getUuid(), pc.getReason()));
                        case WARN ->
                                pp.addEntry(created = Punishment.createWarn(target.getUuid(), playerRef.getUuid(), pc.getReason()));
                    }
                    if (created == null) {
                        throw new IllegalArgumentException("Something went wrong while creating punishment!");
                    }
                    PlayerRef targetPlayerRef = Universe.get().getPlayer(target.getUuid());
                    if (targetPlayerRef == null || targetPlayerRef.getReference() == null) {
                        player.sendMessage(Message.raw("Something went wrong!"));
                        this.close();
                    } else {
                        if (pc.getType() == PunishmentType.BAN || pc.getType() == PunishmentType.KICK) {
                            targetPlayerRef.getPacketHandler().disconnect(created.getReasonMessage().getAnsiMessage());
                        } else {
                            targetPlayerRef.sendMessage(created.getReasonMessage());
                        }
                    }
                    player.sendMessage(created.getSuccessMessage(Message.raw(target.getUsername()).bold(true)));
                } catch (NoPermissionException e) {
                    player.sendMessage(Message.translation(e.getMessage()));
                }
                this.close();
                return;
            case "Edit":
                GuiUtil.sendToErrorPage(player, playerRef, ref, store, target, "tarobits.punishments.system.error.wrongpage");
                return;
            default:
                player.sendMessage(Message.raw("Something went wrong!"));
                this.close();
                return;
        }
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        buildTab(GuiUtil.ListTypes.ADD, this.tabButtonMap, this.selectedTab, this.presetConfigs, ref, uiCommandBuilder, uiEventBuilder, store, playerRef);
        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    @Override
    protected void sendUpdate(@NullableDecl UICommandBuilder commandBuilder, @NullableDecl UIEventBuilder eventBuilder, boolean clear) {
        super.sendUpdate(commandBuilder, eventBuilder, clear);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        if (!Permissions.playerHasAny(playerRef.getUuid(), List.of(
                Permissions.BAN_PERM,
                Permissions.BAN_TEMP,
                Permissions.MUTE_PERM,
                Permissions.MUTE_TEMP,
                Permissions.KICK,
                Permissions.WARN
        ))) {
            playerRef.sendMessage(Message.translation("tarobits.punishments.error.noperm"));
            this.close();
            return;
        }
        uiCommandBuilder.append("Pages/Tarobits_Punishments_ListPunishmentsGui.ui");

        uiCommandBuilder.set("#HeaderTitle.Text", Message.translation("tarobits.punishments.gui.add.title"));
        uiCommandBuilder.set("#PageTitle.Text", target.getUsername());

        GuiUtil.buildPlayerHeader(uiCommandBuilder, historyStats, "#Header");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of(GuiUtil.ListPunishmentsData.ACTION_KEY, "BackButton"));

        buildTabs(GuiUtil.ListTypes.ADD, this.tabButtonMap, this.selectedTab, this.presetConfigs, ref, uiCommandBuilder, uiEventBuilder, store, playerRef);
    }
}
