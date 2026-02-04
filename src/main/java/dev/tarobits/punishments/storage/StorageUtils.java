package dev.tarobits.punishments.storage;

import com.hypixel.hytale.logger.HytaleLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;

public class StorageUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static File createDataFile(
            Path pluginDir,
            String fileName
    ) {
        return createDataFile(pluginDir, fileName, true);
    }

    public static File createDataFile(
            Path pluginDir,
            String fileName,
            Boolean storage
    ) {
        File dataDirectory;
        if (storage) {
            dataDirectory = pluginDir.resolve("storage")
                    .toFile();
        } else {
            dataDirectory = pluginDir.toFile();
        }
        if (!dataDirectory.exists()) {
            LOGGER.at(Level.FINEST)
                    .log("Creating storage directory.");
            if (!dataDirectory.mkdirs()) {
                LOGGER.atSevere()
                        .log("Failed to create storage directory!");
            } else {
                LOGGER.at(Level.FINEST)
                        .log("Successfully created storage directory.");
            }
        }
        return dataDirectory.toPath()
                .resolve(fileName)
                .toFile();
    }
}
