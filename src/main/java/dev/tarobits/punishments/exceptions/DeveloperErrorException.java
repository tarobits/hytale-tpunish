package dev.tarobits.punishments.exceptions;

import dev.tarobits.punishments.TPunish;

// When the dev has messed up
public class DeveloperErrorException extends RuntimeException {
	public DeveloperErrorException(String message) {
		super(message);
		StackTraceElement trace = this.getStackTrace()[0];
		String className = trimClass(trace.getClassName());
		String line = String.valueOf(trace.getLineNumber());
		String method = trace.getMethodName();

		String consoleMessage = "DEVELOPER ERROR\n" + "Message: " + message + "\n" + "Class: " + className + "\n" + "Method: " + method + "\n" + "Line: " + line + "\n";
		TPunish.getLogger(className)
				.atSevere()
				.log(consoleMessage);
	}

	private String trimClass(String fullClass) {
		String[] parts = fullClass.split("\\.");
		return parts[parts.length - 1];
	}
}
