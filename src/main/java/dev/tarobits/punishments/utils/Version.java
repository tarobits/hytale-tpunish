package dev.tarobits.punishments.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public record Version(Integer major, Integer minor, Integer patch) {

    public static Version fromVersionString(String versionString) {
        String[] pieces = versionString.split("\\.");
        return new Version(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
    }

    public Message getUpdateMessage(Version oldVersion) {
        return Message.translation("tarobits.punishments.updatemessage").param("newVersion", Message.raw(this.toString()).color(Color.GREEN).bold(true)).param("oldVersion", Message.raw(oldVersion.toString()).color(Color.RED).bold(true));
    }

    @Override
    @Nonnull
    public String toString() {
        return "v" + this.major + "." + this.minor + "." + this.patch;
    }

    public static Version getNewestVersion() {
        try {
            URL manifest = URI.create("https://raw.githubusercontent.com/tarobits/hytale-tpunish/refs/heads/main/src/main/resources/manifest.json").toURL();
            StringBuilder read = new StringBuilder();
            Scanner scanner = new Scanner(manifest.openStream());

            while (scanner.hasNext()) {
                read.append(scanner.nextLine());
            }

            scanner.close();

            JsonObject obj = JsonParser.parseString(read.toString()).getAsJsonObject();

            return Version.fromVersionString(obj.get("Version").getAsString());
        } catch (Exception _) {
            throw new IllegalArgumentException("Couldn't get version!");
        }
    }

    public Boolean isNewer(Version version) {
        return this.major < version.major || this.minor < version.minor || this.patch < version.patch;
    }
}
