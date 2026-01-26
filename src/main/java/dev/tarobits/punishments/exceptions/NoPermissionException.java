package dev.tarobits.punishments.exceptions;

public class NoPermissionException extends RuntimeException {
    public NoPermissionException() {
        super("tarobits.punishments.error.noperm");
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
