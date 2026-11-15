package io.github.inbox.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCancelledEventUnitTest {

    private static final Instant CANCELLED_AT = Instant.parse("2026-11-14T10:00:00Z");

    @Test
    @DisplayName("UT constructor when orderId is null should throw NullPointerException")
    void constructor_whenOrderIdIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCancelledEvent(null, 42L, "changed my mind", CANCELLED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("orderId cannot be null");
    }

    @Test
    @DisplayName("UT constructor when customerId is null should throw NullPointerException")
    void constructor_whenCustomerIdIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCancelledEvent(1L, null, "changed my mind", CANCELLED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("customerId cannot be null");
    }

    @Test
    @DisplayName("UT constructor when cancelledAt is null should throw NullPointerException")
    void constructor_whenCancelledAtIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCancelledEvent(1L, 42L, "changed my mind", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("cancelledAt cannot be null");
    }

    @Test
    @DisplayName("UT constructor when reason is null should accept an order cancelled without one")
    void constructor_whenReasonIsNull_shouldAcceptEvent() {
        // when
        OrderCancelledEvent tested = new OrderCancelledEvent(1L, 42L, null, CANCELLED_AT);

        // then
        assertThat(tested.reason()).isNull();
    }
}
