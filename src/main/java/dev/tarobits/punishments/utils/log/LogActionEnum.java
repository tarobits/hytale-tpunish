package dev.tarobits.punishments.utils.log;

import com.hypixel.hytale.server.core.Message;
import dev.tarobits.punishments.utils.domainobject.DomainObject;
import dev.tarobits.punishments.utils.ui.UIText;

public interface LogActionEnum<T extends DomainObject<?>> {
	Message getLogActionText(
			T item,
			LogEntry logEntry
	);

	UIText getUIText();
}
