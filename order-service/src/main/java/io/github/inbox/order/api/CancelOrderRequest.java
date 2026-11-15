package io.github.inbox.order.api;

import jakarta.validation.constraints.Size;

/**
 * @param reason why the order is cancelled, or {@code null} when the customer gave none
 */
public record CancelOrderRequest(
        @Size(max = 500, message = "reason must be at most {max} characters")
        String reason
) {
}
