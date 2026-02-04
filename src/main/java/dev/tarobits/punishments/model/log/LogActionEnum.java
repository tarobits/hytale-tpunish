package dev.tarobits.punishments.model.log;

import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.domain.DomainObject;
import dev.tarobits.punishments.ui.UIText;

public interface LogActionEnum<T extends DomainObject<?>> {
	Message getLogActionText(
			T item,
			LogEntry logEntry
	);

	UIText getUIText();
}
