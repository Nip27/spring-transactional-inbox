package io.github.inbox.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Product line of a new order")
public record OrderLineRequest(
        @Schema(description = "Catalogue identifier of the product", example = "sku-1042",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "productId must not be blank")
        @Size(max = 64, message = "productId must be at most {max} characters")
        String productId,

        @Schema(description = "Number of units ordered", example = "2", minimum = "1", maximum = "10000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "quantity must be positive")
        @Max(value = 10_000, message = "quantity must be at most {value}")
        int quantity,

        @Schema(description = "Price of one unit in the currency of the order", example = "10.50",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "unitPrice must not be null")
        @Positive(message = "unitPrice must be positive")
        @Digits(integer = 17, fraction = 2, message = "unitPrice must have at most {fraction} decimal places")
        BigDecimal unitPrice
) {
}
