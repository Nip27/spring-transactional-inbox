package io.github.inbox.loyalty.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Loyalty points balance of a customer")
public record PointsBalanceResponse(
        @Schema(description = "Customer the balance belongs to", example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long customerId,

        @Schema(description = "Points available to the customer", example = "120",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long balance,

        @Schema(description = "When the balance last changed", example = "2026-11-14T10:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Instant updatedAt
) {
}
