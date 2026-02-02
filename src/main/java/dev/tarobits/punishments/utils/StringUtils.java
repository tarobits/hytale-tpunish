package dev.tarobits.punishments.utils;

public class StringUtils {
	public static String toTitleCase(String string) {
		string = string.toLowerCase();
		string = string.replaceAll("_", " ");
		string = string.replaceAll("-", " ");
		StringBuilder result = new StringBuilder();
		char previousChar = 0;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			result.append(Character.isSpaceChar(previousChar) || previousChar == 0 ? Character.toUpperCase(c) : c);
			previousChar = c;
		}
		return result.toString();
	}
}
