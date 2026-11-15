package io.github.inbox.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Published as {@link EventTypes#ORDER_CREATED} in the transaction that stores a new order.
 * <p>
 * The event id is not part of the payload: the outbox assigns it and sends it as a message header, and that header
 * is what consumers deduplicate on.
 *
 * @param orderId       the identifier of the order in the order service
 * @param customerId    the customer who placed the order
 * @param customerEmail where the order confirmation is sent
 * @param lines         the ordered products, never empty
 * @param totalAmount   the sum of all lines
 * @param currency      the ISO 4217 code of the amounts
 * @param createdAt     when the order was stored
 */
public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        String customerEmail,
        List<OrderLine> lines,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {

    public OrderCreatedEvent {
        Objects.requireNonNull(orderId, "orderId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(customerEmail, "customerEmail cannot be null");
        Objects.requireNonNull(lines, "lines cannot be null");
        Objects.requireNonNull(totalAmount, "totalAmount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines cannot be empty");
        }
        lines = List.copyOf(lines);
    }
}
