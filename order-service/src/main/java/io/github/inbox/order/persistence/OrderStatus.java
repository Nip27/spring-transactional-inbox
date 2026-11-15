package io.github.inbox.order.persistence;

/**
 * Lifecycle of an order. An order is created in {@link #CREATED} and can move to {@link #CANCELLED} once; a
 * cancelled order does not come back.
 */
public enum OrderStatus {
    CREATED,
    CANCELLED
}
