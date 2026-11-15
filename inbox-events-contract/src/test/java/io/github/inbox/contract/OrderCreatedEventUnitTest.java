package io.github.inbox.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCreatedEventUnitTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-14T10:00:00Z");
    private static final List<OrderLine> LINES = List.of(new OrderLine("sku-1", 2, new BigDecimal("10.50")));

    @Test
    @DisplayName("UT constructor when orderId is null should throw NullPointerException")
    void constructor_whenOrderIdIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(null, 42L, "a@b.c", LINES, BigDecimal.TEN, "EUR", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("orderId cannot be null");
    }

    @Test
    @DisplayName("UT constructor when customerId is null should throw NullPointerException")
    void constructor_whenCustomerIdIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, null, "a@b.c", LINES, BigDecimal.TEN, "EUR", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("customerId cannot be null");
    }

    @Test
    @DisplayName("UT constructor when customerEmail is null should throw NullPointerException")
    void constructor_whenCustomerEmailIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, null, LINES, BigDecimal.TEN, "EUR", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("customerEmail cannot be null");
    }

    @Test
    @DisplayName("UT constructor when lines is null should throw NullPointerException")
    void constructor_whenLinesIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, "a@b.c", null, BigDecimal.TEN, "EUR", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("lines cannot be null");
    }

    @Test
    @DisplayName("UT constructor when totalAmount is null should throw NullPointerException")
    void constructor_whenTotalAmountIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, "a@b.c", LINES, null, "EUR", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("totalAmount cannot be null");
    }

    @Test
    @DisplayName("UT constructor when currency is null should throw NullPointerException")
    void constructor_whenCurrencyIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, "a@b.c", LINES, BigDecimal.TEN, null, CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currency cannot be null");
    }

    @Test
    @DisplayName("UT constructor when createdAt is null should throw NullPointerException")
    void constructor_whenCreatedAtIsNull_shouldThrowNullPointerException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, "a@b.c", LINES, BigDecimal.TEN, "EUR", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("createdAt cannot be null");
    }

    @Test
    @DisplayName("UT constructor when lines are empty should reject the event")
    void constructor_whenLinesAreEmpty_shouldThrowIllegalArgumentException() {
        // when / then
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 42L, "a@b.c", List.of(), BigDecimal.TEN, "EUR", CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lines cannot be empty");
    }

    @Test
    @DisplayName("UT constructor when the caller later changes its list should keep the lines it was given")
    void constructor_whenCallerChangesItsList_shouldKeepOriginalLines() {
        // given
        List<OrderLine> lines = new ArrayList<>(LINES);
        OrderCreatedEvent tested = new OrderCreatedEvent(1L, 42L, "a@b.c", lines, BigDecimal.TEN, "EUR", CREATED_AT);

        // when
        lines.clear();

        // then
        assertThat(tested.lines()).containsExactlyElementsOf(LINES);
    }
}
