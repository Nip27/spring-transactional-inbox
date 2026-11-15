package io.github.inbox.notification.events;

/**
 * A message that does not carry a usable event id. Retrying cannot repair it, so it goes to the dead letter topic at
 * once instead of being retried.
 */
public class MalformedEventException extends RuntimeException {

    public MalformedEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
