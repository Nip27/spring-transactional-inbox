package io.github.inbox.contract;

/**
 * Names of the order event types. A type is the key under which the producer's outbox is configured and the
 * consumers' inbox maps a payload class, so both sides read it from here and cannot disagree on it.
 */
public final class EventTypes {

    public static final String ORDER_CREATED = "order-created";
    public static final String ORDER_CANCELLED = "order-cancelled";

    private EventTypes() {
    }
}
