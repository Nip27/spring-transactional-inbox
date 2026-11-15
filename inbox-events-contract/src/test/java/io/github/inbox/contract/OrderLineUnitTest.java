package io.github.inbox.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderLineUnitTest {

    @Test
    @DisplayName("UT constructor when productId is null should throw NullPointerException")
    void constructor_whenProductIdIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderLine(null, 1, BigDecimal.TEN))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("productId cannot be null");
    }

    @Test
    @DisplayName("UT constructor when unitPrice is null should throw NullPointerException")
    void constructor_whenUnitPriceIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderLine("sku-1", 1, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("unitPrice cannot be null");
    }

    @Test
    @DisplayName("UT constructor when quantity is zero should reject it")
    void constructor_whenQuantityIsZero_shouldThrowIllegalArgumentException() {
        // when / then
        assertThatThrownBy(() -> new OrderLine("sku-1", 0, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be at least 1, was 0");
    }
}
