package dev.tarobits.punishments.utils;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

public class TranslationUtil {
	public static Message getMessage(@Nonnull String message) {
		if (message.startsWith("tarobits.punishments.")) {
			return Message.translation(message);
		}
		return Message.raw(message);
	}

	public static String getString(@Nonnull String message) {
		return getMessage(message).getAnsiMessage();
	}
}
