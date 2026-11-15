package io.github.inbox.order.api;

import io.github.inbox.order.persistence.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Order")
public record OrderResponse(
        @Schema(description = "Id of the order", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Customer who placed the order", example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long customerId,

        @Schema(description = "Address the order confirmation is sent to", example = "jane.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String customerEmail,

        @Schema(description = "Where the order is in its lifecycle", example = "CREATED",
                requiredMode = Schema.RequiredMode.REQUIRED)
        OrderStatus status,

        @Schema(description = "ISO 4217 code of the amounts", example = "EUR",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String currency,

        @Schema(description = "Sum of all lines", example = "21.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal totalAmount,

        @Schema(description = "Products ordered", requiredMode = Schema.RequiredMode.REQUIRED)
        List<OrderLineResponse> lines,

        @Schema(description = "When the order was placed", example = "2026-11-14T10:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Instant createdAt,

        @Schema(description = "When the order was cancelled, absent while it is not", example = "2026-11-14T11:00:00Z",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant cancelledAt,

        @Schema(description = "Why the order was cancelled, absent when it is not or no reason was given",
                example = "Ordered by mistake", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String cancelReason
) {
}
