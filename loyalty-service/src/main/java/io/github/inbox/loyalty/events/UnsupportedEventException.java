package io.github.inbox.loyalty.events;

/**
 * An event this service has no handling for. Retrying cannot help, so it goes to the dead letter topic at once.
 */
public class UnsupportedEventException extends RuntimeException {

    public UnsupportedEventException(Object payload) {
        super("No handling for event payload %s".formatted(payload == null ? null : payload.getClass().getName()));
    }
}
