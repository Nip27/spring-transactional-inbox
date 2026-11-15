package io.github.inbox.order.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product line of an order")
public record OrderLineResponse(
        @Schema(description = "Catalogue identifier of the product", example = "sku-1042",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String productId,

        @Schema(description = "Number of units ordered", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity,

        @Schema(description = "Price of one unit in the currency of the order", example = "10.50",
                requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal unitPrice
) {
}
