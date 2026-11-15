package io.github.inbox.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Order to place")
public record CreateOrderRequest(
        @Schema(description = "Customer placing the order", example = "42", minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "customerId must not be null")
        @Positive(message = "customerId must be positive")
        Long customerId,

        @Schema(description = "Address the order confirmation is sent to", example = "jane.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "customerEmail must not be blank")
        @Email(message = "customerEmail must be a well-formed e-mail address")
        @Size(max = 320, message = "customerEmail must be at most {max} characters")
        String customerEmail,

        @Schema(description = "ISO 4217 code of the amounts", example = "EUR", pattern = "^[A-Z]{3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "currency must not be null")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be an ISO 4217 code of three capital letters")
        String currency,

        @Schema(description = "Products ordered", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "lines must not be null")
        @Size(min = 1, max = 50, message = "lines must hold between {min} and {max} items")
        List<@Valid @NotNull(message = "lines must not contain null") OrderLineRequest> lines
) {
}
