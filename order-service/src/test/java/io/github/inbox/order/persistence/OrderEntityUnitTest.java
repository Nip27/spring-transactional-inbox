package io.github.inbox.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderEntityUnitTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-14T10:00:00Z");
    private static final Instant CANCELLED_AT = Instant.parse("2026-11-14T11:00:00Z");

    @Test
    @DisplayName("UT constructor when order has several lines should sum their amounts into the total")
    void constructor_whenOrderHasSeveralLines_shouldSumLineAmounts() {
        // when
        OrderEntity tested = newOrder();

        // then
        assertThat(tested.getTotalAmount()).isEqualByComparingTo("31.00");
        assertThat(tested.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(tested.getLines()).allSatisfy(line -> assertThat(line.getOrder()).isSameAs(tested));
    }

    @Test
    @DisplayName("UT cancel() when order is created should store the reason and the time")
    void cancel_whenOrderIsCreated_shouldMoveToCancelled() {
        // given
        OrderEntity tested = newOrder();

        // when
        tested.cancel("changed my mind", CANCELLED_AT);

        // then
        assertThat(tested.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(tested.getCancelReason()).isEqualTo("changed my mind");
        assertThat(tested.getCancelledAt()).isEqualTo(CANCELLED_AT);
    }

    @Test
    @DisplayName("UT cancel() when order is already cancelled should refuse and keep the first cancellation")
    void cancel_whenOrderIsAlreadyCancelled_shouldThrowIllegalStateException() {
        // given
        OrderEntity tested = newOrder();
        tested.cancel("first", CANCELLED_AT);

        // when / then
        assertThatThrownBy(() -> tested.cancel("second", CANCELLED_AT.plusSeconds(60)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tested.getCancelReason()).isEqualTo("first");
        assertThat(tested.getCancelledAt()).isEqualTo(CANCELLED_AT);
    }

    @Test
    @DisplayName("UT getLines() when called should not let the caller change the lines")
    void getLines_whenCalled_shouldReturnUnmodifiableList() {
        // given
        OrderEntity tested = newOrder();

        // when / then
        assertThatThrownBy(() -> tested.getLines().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static OrderEntity newOrder() {
        return new OrderEntity(
                42L,
                "customer@example.com",
                "EUR",
                List.of(
                        new OrderLineEntity("sku-1", 2, new BigDecimal("10.50")),
                        new OrderLineEntity("sku-2", 1, new BigDecimal("10.00"))
                ),
                CREATED_AT
        );
    }
}
