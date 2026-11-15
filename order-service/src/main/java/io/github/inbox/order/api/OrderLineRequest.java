package io.github.inbox.order.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderLineRequest(
        @NotBlank(message = "productId must not be blank")
        @Size(max = 64, message = "productId must be at most {max} characters")
        String productId,

        @Positive(message = "quantity must be positive")
        int quantity,

        @NotNull(message = "unitPrice must not be null")
        @Positive(message = "unitPrice must be positive")
        @Digits(integer = 17, fraction = 2, message = "unitPrice must have at most {fraction} decimal places")
        BigDecimal unitPrice
) {
}
