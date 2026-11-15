package io.github.inbox.contract;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One product line of an order, as it was priced when the order was placed.
 *
 * @param productId the catalogue identifier of the product
 * @param quantity  how many units were ordered, at least one
 * @param unitPrice the price of one unit in the currency of the order
 */
public record OrderLine(String productId, int quantity, BigDecimal unitPrice) {

    public OrderLine {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1, was %d".formatted(quantity));
        }
    }
}
