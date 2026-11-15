package io.github.inbox.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * @param reason why the order is cancelled, or {@code null} when the customer gave none
 */
@Schema(description = "Cancellation of an order")
public record CancelOrderRequest(
        @Schema(description = "Why the order is cancelled", example = "Ordered by mistake",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 500, message = "reason must be at most {max} characters")
        String reason
) {
}
