package dev.tarobits.punishments.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import dev.tarobits.punishments.model.punishment.Punishment;
import dev.tarobits.punishments.model.punishment.PunishmentSubtype;
import dev.tarobits.punishments.model.punishment.PunishmentType;

import javax.annotation.Nullable;

public class PunishmentEntryBuilder {

    public static UIText buildStatusText(
            @Nullable Punishment punishment,
            String displayText
    ) {
        Value<String> activeStyle = Value.ref("TPunish_Styles/Punishment.ui", "Active");
        Value<String> expiredStyle = Value.ref("TPunish_Styles/Punishment.ui", "Expired");
        if (punishment == null) {
            return new UIText(
                    Message.translation(
                                    "tarobits.punishments.gui.header." + displayText.toLowerCase() + ".not" + displayText.toLowerCase())
                            .getAnsiMessage(), expiredStyle
            );
        }
        if (punishment.getType() != PunishmentType.BAN && punishment.getType() != PunishmentType.MUTE) {
            return new UIText("", activeStyle);
        }
        if (punishment.isActive() && !punishment.isPardoned()) {
            StringBuilder text = new StringBuilder();
            text.append(displayText)
                    .append(" ");

            if (punishment.getSubtype() == PunishmentSubtype.PERMANENT) {
                text.append(Message.translation("tarobits.punishments.gui.header.indefinitely")
                                    .getAnsiMessage());
            } else {
                text.append(Message.translation("tarobits.punishments.gui.header.until")
                                    .getAnsiMessage())
                        .append(" ")
                        .append(punishment.getUntil());
            }
            return new UIText(text.toString(), activeStyle);
        }

        return new UIText(
                Message.translation(
                                "tarobits.punishments.gui.header." + punishment.getTranslationKey() + ".not" + punishment.getTranslationKey())
                        .getAnsiMessage(), expiredStyle
        );
    }
}
