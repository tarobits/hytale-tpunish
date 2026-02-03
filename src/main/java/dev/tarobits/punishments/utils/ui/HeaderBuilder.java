package dev.tarobits.punishments.utils.ui;

import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;

import java.util.List;

public class HeaderBuilder {

	private static final Value<String> defaultGroupStyle = Value.ref(
			"TPunish_Styles/Header/General.ui", "DefaultHeaderGroupTitleStyle");
	private static final Value<String> defaultKeyStyle = Value.ref(
			"TPunish_Styles/Header/General.ui", "DefaultHeaderKeyStyle");
	private static final Value<String> defaultValueStyle = Value.ref(
			"TPunish_Styles/Header/General.ui", "DefaultHeaderValueStyle");

	public static void buildHeader(
			List<HeaderGroup> headerConfig,
			UICommandBuilder uiCommandBuilder
	) {
		if (headerConfig.size() > 3) {
			throw new DeveloperErrorException("Headers cannot have more than 3 elements");
		}
		if (headerConfig.isEmpty()) {
			throw new DeveloperErrorException("Headers cannot have empty elements");
		}

		String headerSelector = "#Header";
		for (int i = 3; i > headerConfig.size(); i--) {
			uiCommandBuilder.set(headerSelector + " #G" + i + ".Visible", false);
			uiCommandBuilder.set(headerSelector + " #S" + i + ".Visible", false);
		}
		for (int i = 0; i < headerConfig.size(); i++) {
			String groupSelector = headerSelector + " #G" + (i + 1);
			HeaderGroup headerGroup = headerConfig.get(i);
			setText(uiCommandBuilder, groupSelector + " #Title", headerGroup.title);
			for (int j = 0; j < headerGroup.elements()
					.size(); j++) {
				HeaderElement element = headerGroup.elements()
						.get(j);
				String elementsGroupSelector = groupSelector + " #Elements";
				String elementSelector = elementsGroupSelector + "[" + (j) + "]";
				uiCommandBuilder.append(elementsGroupSelector, "Tarobits_Punishments_HeaderElement.ui");
				setText(uiCommandBuilder, elementSelector + " #Key", element.key);
				setText(uiCommandBuilder, elementSelector + " #Val", element.value);
			}
		}
	}

	private static void setText(
			UICommandBuilder uiCommandBuilder,
			String selector,
			UIText headerText
	) {
		uiCommandBuilder.set(selector + ".Text", headerText.text());
		uiCommandBuilder.set(selector + ".Style", headerText.style());
	}

	public record HeaderGroup(UIText title, List<HeaderElement> elements) {
		public HeaderGroup(
				String title,
				List<HeaderElement> elements
		) {
			this(new UIText(title, defaultGroupStyle), elements);
		}
	}

	public record HeaderElement(UIText key, UIText value) {
		public HeaderElement(
				String key,
				UIText value
		) {
			this(new UIText(key, defaultKeyStyle), value);
		}

		public HeaderElement(
				UIText key,
				String value
		) {
			this(key, new UIText(value, defaultValueStyle));
		}

		public HeaderElement(
				String key,
				String value
		) {
			this(new UIText(key, defaultKeyStyle), new UIText(value, defaultValueStyle));
		}
	}
}
