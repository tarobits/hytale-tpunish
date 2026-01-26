package dev.tarobits.punishments.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.tarobits.punishments.provider.PunishmentProvider;
import dev.tarobits.punishments.utils.Permissions;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentType;
import dev.tarobits.punishments.utils.ui.HistoryStat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PunishmentsGui extends InteractiveCustomUIPage<PunishmentsGui.PunishGuiData> {
    private final ProfileServiceClient.PublicGameProfile target;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final PunishmentProvider pp = PunishmentProvider.get();
    private final Map<PunishmentType, HistoryStat> historyStats = new Object2ObjectOpenHashMap<>();

    public PunishmentsGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, @Nonnull ProfileServiceClient.PublicGameProfile target) {
        super(playerRef, lifetime, PunishGuiData.CODEC);
        this.target = target;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        if (!Permissions.playerHas(playerRef.getUuid(), Permissions.OPEN_GUI)) {
            playerRef.sendMessage(Message.translation("tarobits.punishments.error.noperm"));
            this.close();
            return;
        }
        uiCommandBuilder.append("Pages/Tarobits_Punishments_PunishmentsGui.ui");

        uiCommandBuilder.set("#Username.Text", this.target.getUsername());
        buildLists(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    protected void handleRemoveButton(@Nonnull PunishGuiData data) {
        switch (data.type) {
            /*case "warn":
                php.pardonSpecificHistoryEntry(this.playerRef.getUuid(), HistoryTypes.WARN, data.id);
                break;*/
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PunishGuiData data) {
        super.handleDataEvent(ref, store, data);
        if (data.button == null) {
            return;
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null || player == null) {
            throw new IllegalArgumentException("Player not found!");
        }

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        switch (data.button) {
            case "RemoveButton":
                handleRemoveButton(data);
                return;
            case "UnbanButton":
                if (!pp.hasBan(target.getUuid())) {
                    GuiUtil.sendToErrorPage(player, playerRef, ref, store, target, "tarobits.punishments.ban.error.isnt");
                    return;
                }
                player.sendMessage(pp.getActive(target.getUuid(), PunishmentType.BAN).pardon());
                break;
            case "UnmuteButton":
                if (!pp.hasMute(target.getUuid())) {
                    GuiUtil.sendToErrorPage(player, playerRef, ref, store, target, "tarobits.punishments.mute.error.isnt");
                    return;
                }
                player.sendMessage(pp.getActive(target.getUuid(), PunishmentType.MUTE).pardon());
                break;
            case "NewPunishButton":
                player.getPageManager().openCustomPage(ref, store, new AddPunishmentGui(playerRef, CustomPageLifetime.CanDismiss, target, historyStats));
                return;
            default:
                LOGGER.atWarning().log(data.button + " is not a valid button!");
                break;
        }
        this.build(ref, uiCommandBuilder, uiEventBuilder, store);
        this.sendUpdate(uiCommandBuilder, uiEventBuilder, true);
    }

    private void buildLists(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {

        List<Punishment> banList = pp.getEntries(target.getUuid(), PunishmentType.BAN).reversed();
        List<Punishment> muteList = pp.getEntries(target.getUuid(), PunishmentType.MUTE).reversed();
        List<Punishment> warnList = pp.getEntries(target.getUuid(), PunishmentType.WARN).reversed();
        List<Punishment> kickList = pp.getEntries(target.getUuid(), PunishmentType.KICK).reversed();

        historyStats.put(PunishmentType.BAN, new HistoryStat(banList.size(), banList.isEmpty() ? null : banList.getFirst()));
        historyStats.put(PunishmentType.MUTE, new HistoryStat(muteList.size(), muteList.isEmpty() ? null : muteList.getFirst()));
        historyStats.put(PunishmentType.WARN, new HistoryStat(warnList.size(), warnList.isEmpty() ? null : warnList.getFirst()));
        historyStats.put(PunishmentType.KICK, new HistoryStat(kickList.size(), kickList.isEmpty() ? null : kickList.getFirst()));

        GuiUtil.buildPlayerHeader(uiCommandBuilder, historyStats, "#Header");

        GuiUtil.buildPunishmentList(uiCommandBuilder, banList, "#BanList", "Ban", false, true, false);

        GuiUtil.buildPunishmentList(uiCommandBuilder, muteList, "#MuteList", "Mute", false, true, false);

        GuiUtil.buildPunishmentList(uiCommandBuilder, warnList, "#WarnList", "Warn", false, false, false);

        GuiUtil.buildPunishmentList(uiCommandBuilder, kickList, "#KickList", "Kick", false, false, false);

        checkIfNecessary(uiCommandBuilder);
        checkPermissions(ref, uiCommandBuilder, store);
        setEvents(uiEventBuilder);
        // ToDo: MAKE Custom Punishment possible
        uiCommandBuilder.set("#CustomPunishButton.Text", "Coming soon");
        uiCommandBuilder.set("#CustomPunishButton.Disabled", true);
    }

    private void checkIfNecessary(@Nonnull UICommandBuilder uiCommandBuilder) {
        Punishment newestBan = historyStats.get(PunishmentType.BAN).latestPunishment;
        if (newestBan == null || !newestBan.isActive() || newestBan.isPardoned()) {
            uiCommandBuilder.set("#UnbanButton.Disabled", true);
        }
        Punishment newestMute = historyStats.get(PunishmentType.MUTE).latestPunishment;
        if (newestMute == null || !newestMute.isActive() || newestMute.isPardoned()) {
            uiCommandBuilder.set("#UnmuteButton.Disabled", true);
        }
    }

    private void checkPermissions(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull Store<EntityStore> store) {
        UUID uuid = playerRef.getUuid();
        if (!Permissions.playerHas(uuid, Permissions.UNMUTE)) {
            uiCommandBuilder.set("#UnmuteButton.Disabled", true);
        }
        if (!Permissions.playerHas(uuid, Permissions.UNBAN)) {
            uiCommandBuilder.set("#UnbanButton.Disabled", true);
        }
        if (!Permissions.playerHas(uuid, Permissions.CUSTOM)) {
            uiCommandBuilder.set("#CustomPunishButton.Disabled", true);
        }
        if (!Permissions.playerHasAny(uuid, List.of(
                Permissions.BAN_PERM,
                Permissions.BAN_TEMP,
                Permissions.MUTE_PERM,
                Permissions.MUTE_TEMP,
                Permissions.KICK,
                Permissions.WARN
        ))) {
            uiCommandBuilder.set("#NewPunishButton.Disabled", true);
        }
    }

    private void setEvents(@Nonnull UIEventBuilder uiEventBuilder) {
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#NewPunishButton", EventData.of(PunishGuiData.BUTTON_KEY, "NewPunishButton"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#UnbanButton", EventData.of(PunishGuiData.BUTTON_KEY, "UnbanButton"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#UnmuteButton", EventData.of(PunishGuiData.BUTTON_KEY, "UnmuteButton"));
    }
    public static class PunishGuiData {
        static final String BUTTON_KEY = "Button";
        static final String TYPE_KEY = "TYPE";
        static final String ID_KEY = "ID";
        private String button;
        private String type;
        private int id;

        public static final BuilderCodec<PunishGuiData> CODEC = BuilderCodec.builder(PunishGuiData.class, PunishGuiData::new)
                .append(new KeyedCodec<>(BUTTON_KEY, Codec.STRING), (data, u) -> data.button = u, (data) -> data.button).add()
                .append(new KeyedCodec<>(TYPE_KEY, Codec.STRING), (data, u) -> data.type = u, (data) -> data.type).add()
                .append(new KeyedCodec<>(ID_KEY, Codec.STRING), (data, u) -> data.id = Integer.parseInt(u), String::valueOf).add()
                .build();
    }
}
