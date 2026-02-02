package dev.tarobits.punishments.exceptions;

public class NoPermissionException extends UserException {
	public NoPermissionException() {
		super("tarobits.punishments.error.noperm");
	}
}
