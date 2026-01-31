package dev.tarobits.punishments.utils.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import dev.tarobits.punishments.utils.punishment.Punishment;
import dev.tarobits.punishments.utils.punishment.PunishmentSubtype;
import dev.tarobits.punishments.utils.punishment.PunishmentType;

import javax.annotation.Nullable;
import java.util.UUID;

public class PunishmentEntryBuilder {

    public static String getActorName(UUID actor) {
        if (actor.toString()
                .equals("00000000-0000-0000-0000-000000000000")) {
            return "Console";
        }
        ServerAuthManager authManager = ServerAuthManager.getInstance();
        String sessionToken = authManager.getSessionToken();

        if (sessionToken == null) {
            return actor.toString();
        }

        ProfileServiceClient profileServiceClient = authManager.getProfileServiceClient();
        var profile = profileServiceClient.getProfileByUuid(actor, sessionToken);
        return profile != null ? profile.getUsername() : "Failed!";
    }

    public static String buildStatusText(
            @Nullable Punishment punishment,
            String displayText
    ) {
        if (punishment == null) {
            return "";
        }
        if (punishment.getType() != PunishmentType.BAN && punishment.getType() != PunishmentType.MUTE) {
            return "";
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
            return text.toString();
        }
        return "";
    }
}
