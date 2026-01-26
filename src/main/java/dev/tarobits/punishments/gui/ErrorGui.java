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
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ErrorGui extends InteractiveCustomUIPage<ErrorGui.ErrorGuiData> {
    @Nullable
    private final ProfileServiceClient.PublicGameProfile target;
    private final Player player;
    private final String errorMessageId;

    public ErrorGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, @Nullable ProfileServiceClient.PublicGameProfile target, @Nonnull String errorMessageId, @Nonnull Player player) {
        super(playerRef, lifetime, ErrorGuiData.CODEC);
        this.target = target;
        this.errorMessageId = errorMessageId;
        this.player = player;
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl ErrorGuiData data) {
        super.handleDataEvent(ref, store, data);
        if (Objects.equals(data.button, "Home")) {
            if (target != null) {
                player.getPageManager().openCustomPage(ref, store, new PunishmentsGui(this.playerRef, CustomPageLifetime.CanDismiss, target));
            } else {
                player.getPageManager().openCustomPage(ref, store, new ManagementGui(playerRef, CustomPageLifetime.CanDismiss));
            }
        }
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Tarobits_Punishments_Error_Page.ui");
        uiCommandBuilder.set("#ErrorText.Text", Message.translation(errorMessageId));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HomeButton", EventData.of(ErrorGuiData.BUTTON_KEY, "Home"));
    }

    public static class ErrorGuiData {
        public static final String BUTTON_KEY = "Button";
        private String button;

        public static final BuilderCodec<ErrorGuiData> CODEC = BuilderCodec.<ErrorGuiData>builder(ErrorGuiData.class, ErrorGuiData::new)
                .append(new KeyedCodec<String>(BUTTON_KEY, Codec.STRING), (d, v) -> d.button = v, (d) -> d.button).add()
                .build();
    }
}
