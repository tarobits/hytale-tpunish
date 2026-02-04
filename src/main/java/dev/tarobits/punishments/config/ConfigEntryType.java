package dev.tarobits.punishments.config;

public enum ConfigEntryType {
	BOOLEAN,
	INTEGER,
	DECIMAL,
	PRESETS(true);

	public final boolean hasSpecialEditor;

	ConfigEntryType() {
		this.hasSpecialEditor = false;
	}

	ConfigEntryType(boolean hasSpecialEditor) {
		this.hasSpecialEditor = hasSpecialEditor;
	}
}
