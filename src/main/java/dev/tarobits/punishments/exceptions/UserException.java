package dev.tarobits.punishments.exceptions;

import com.hypixel.hytale.server.core.Message;

public class UserException extends Exception {
	public UserException(String message) {
		super(message);
	}

	public Message getChatMessage() {
		if (this.getMessage()
				.startsWith("tarobits.punishments")) {
			return Message.translation(this.getMessage());
		}
		return Message.raw(this.getMessage());
	}

	public String getTextMessage() {
		return this.getChatMessage()
				.getAnsiMessage();
	}
}
