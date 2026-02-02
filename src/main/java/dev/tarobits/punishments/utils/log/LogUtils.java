package dev.tarobits.punishments.utils.log;

import com.hypixel.hytale.server.core.Message;

public class LogUtils {
	public static Message prepareMessage(
			String translation,
			String target,
			String prevValue,
			String newValue,
			String diff
	) {
		return Message.translation(translation)
				.param("target", target)
				.param("prevValue", prevValue)
				.param("newValue", newValue)
				.param("diff", diff);
	}
}
