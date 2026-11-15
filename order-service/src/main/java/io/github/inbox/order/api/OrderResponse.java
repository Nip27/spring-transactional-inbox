package io.github.inbox.order.api;

import io.github.inbox.order.persistence.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * An order as the API shows it.
 *
 * @param id            the identifier of the order
 * @param customerId    the customer who placed the order
 * @param customerEmail where the order confirmation goes
 * @param status        where the order is in its lifecycle
 * @param currency      the ISO 4217 code of the amounts
 * @param totalAmount   the sum of all lines
 * @param lines         the ordered products
 * @param createdAt     when the order was placed
 * @param cancelledAt   when the order was cancelled, or {@code null} while it is not
 * @param cancelReason  why the order was cancelled, or {@code null} when it is not or no reason was given
 */
public record OrderResponse(
        Long id,
        Long customerId,
        String customerEmail,
        OrderStatus status,
        String currency,
        BigDecimal totalAmount,
        List<OrderLineResponse> lines,
        Instant createdAt,
        Instant cancelledAt,
        String cancelReason
) {
}
