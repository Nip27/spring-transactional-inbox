package io.github.inbox.order.api;

import java.math.BigDecimal;

public record OrderLineResponse(String productId, int quantity, BigDecimal unitPrice) {
}
