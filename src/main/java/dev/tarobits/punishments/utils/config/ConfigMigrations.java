package dev.tarobits.punishments.utils.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.tarobits.punishments.provider.ConfigProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Collection;
import java.util.List;

public class ConfigMigrations {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static void log(String msg) {
        LOGGER.atInfo().log(msg);
    }

    public static JsonObject performMigrations(JsonObject jsonObject, Integer currentVersion) {
        log("-=-=-= Running config migrations =-=-=-");
        if (currentVersion == 0) {
            log("Running migration to config version 1");
            jsonObject = zeroToOne(jsonObject);
            currentVersion = 1;
        }

        // Add more version migrations here

        log("-=-=-= Config migrations END =-=-=-");
        return jsonObject;
    }

    private static JsonObject zeroToOne(JsonObject jsonObject) {
        JsonObject result = new JsonObject();

        try {
            result.addProperty("doPunishmentNotifications", jsonObject.get("DoPunishmentNotifications").getAsBoolean());
        } catch (Exception _) {
            result.addProperty("doPunishmentNotifications", true);
        }

        try {
            result.addProperty("showUpdateNotifications", jsonObject.get("ShowUpdateNotifications").getAsBoolean());
        } catch (Exception _) {
            result.addProperty("showUpdateNotifications", true);
        }

        try {
            JsonArray newPresets = new JsonArray();
            jsonObject.get("Presets").getAsJsonArray().forEach((e) -> {
                JsonObject obj = e.getAsJsonObject();
                JsonObject newObj = new JsonObject();

                newObj.addProperty("name", obj.get("Name").getAsString());
                newObj.addProperty("duration", obj.get("Duration").getAsString());
                newObj.addProperty("type", obj.get("Type").getAsString());
                newObj.addProperty("sub_type", obj.get("SubType").getAsString());
                newObj.addProperty("reason", obj.get("Reason").getAsString());

                newPresets.add(newObj);
            });

            result.add("presets", newPresets);
        } catch (Exception _) {
            JsonArray presetArray = getDefaultPresetsV1();
            result.add("presets", presetArray);
        }

        return result;
    }

    @NonNullDecl
    private static JsonArray getDefaultPresetsV1() {
        JsonArray presetArray = new JsonArray();
        JsonObject pre1 = new JsonObject();
        pre1.addProperty("name", "Hacking");
        pre1.addProperty("duration", "30d");
        pre1.addProperty("type", "ban");
        pre1.addProperty("sub_type", "temp");
        pre1.addProperty("reason", "Hacking");
        JsonObject pre2 = new JsonObject();
        pre2.addProperty("name", "Hacking 2nd Offense");
        pre2.addProperty("duration", "3m");
        pre2.addProperty("type", "ban");
        pre2.addProperty("sub_type", "temp");
        pre2.addProperty("reason", "Hacking");
        presetArray.add(pre1);
        presetArray.add(pre2);
        return presetArray;
    }
}
