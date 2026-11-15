package io.github.inbox.contract;

/**
 * Kafka topics the order events travel through.
 */
public final class Topics {

    /**
     * Every order event type, keyed by nothing: events of one order may land on different partitions, so
     * consumers do not rely on their order.
     */
    public static final String ORDER_EVENTS = "orders.events";

    private Topics() {
    }
}
