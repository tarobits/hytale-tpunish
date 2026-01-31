package dev.tarobits.punishments.gui;

import com.hypixel.hytale.codec.Codec;
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
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class ManagementGui extends InteractiveCustomUIPage<ManagementGui.ManagementGuiData> {

    public ManagementGui(
            @Nonnull PlayerRef playerRef,
            @Nonnull CustomPageLifetime lifetime
    ) {
        super(playerRef, lifetime, ManagementGuiData.CODEC);
    }

    @Override
    public void handleDataEvent(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl ManagementGuiData data
    ) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null || player == null) {
            throw new IllegalArgumentException("Player not found!");
        }

        if (data.action == null) {
            return;
        }
        if (data.action.equals("EditPresets")) {
            player.getPageManager()
                    .openCustomPage(ref, store, new EditPresetsGui(playerRef, CustomPageLifetime.CanDismiss));
        }
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        uiCommandBuilder.append("Pages/Tarobits_Punishments_ManagementGui.ui");

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#EditPresetsButton",
                EventData.of(ManagementGuiData.ACTION_KEY, "EditPresets")
        );
    }

    public static class ManagementGuiData {
        private static final String ACTION_KEY = "Action";
        public static final BuilderCodec<ManagementGuiData> CODEC = BuilderCodec.builder(
                        ManagementGuiData.class,
                        ManagementGuiData::new
                )
                .append(new KeyedCodec<String>(ACTION_KEY, Codec.STRING), (d, v) -> d.action = v, (d) -> d.action)
                .add()
                .build();
        private String action;
    }
}
