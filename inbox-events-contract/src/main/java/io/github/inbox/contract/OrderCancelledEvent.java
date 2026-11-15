package io.github.inbox.contract;

import java.time.Instant;
import java.util.Objects;

/**
 * Published as {@link EventTypes#ORDER_CANCELLED} in the transaction that cancels an order.
 *
 * @param orderId     the identifier of the cancelled order
 * @param customerId  the customer who placed the order
 * @param reason      why the order was cancelled, or {@code null} when none was given
 * @param cancelledAt when the order was cancelled
 */
public record OrderCancelledEvent(Long orderId, Long customerId, String reason, Instant cancelledAt) {

    public OrderCancelledEvent {
        Objects.requireNonNull(orderId, "orderId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(cancelledAt, "cancelledAt cannot be null");
    }
}
