package io.github.inbox.order.persistence;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecycle of an order. An order is created in {@link #CREATED} and can move to {@link #CANCELLED} once; a
 * cancelled order does not come back.
 */
@Schema(description = "Order state: CREATED - placed and active, CANCELLED - cancelled, final", enumAsRef = true)
public enum OrderStatus {
    CREATED,
    CANCELLED
}
