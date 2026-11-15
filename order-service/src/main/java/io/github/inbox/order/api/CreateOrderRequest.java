package io.github.inbox.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "customerId must not be null")
        @Positive(message = "customerId must be positive")
        Long customerId,

        @NotBlank(message = "customerEmail must not be blank")
        @Email(message = "customerEmail must be a well-formed e-mail address")
        @Size(max = 320, message = "customerEmail must be at most {max} characters")
        String customerEmail,

        @NotNull(message = "currency must not be null")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be an ISO 4217 code of three capital letters")
        String currency,

        @NotEmpty(message = "lines must not be empty")
        @Size(max = 50, message = "lines must hold at most {max} items")
        List<@Valid @NotNull(message = "lines must not contain null") OrderLineRequest> lines
) {
}
