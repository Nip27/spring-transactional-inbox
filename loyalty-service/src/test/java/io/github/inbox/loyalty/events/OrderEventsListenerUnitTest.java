package io.github.inbox.loyalty.events;

import io.github.inbox.contract.OrderCancelledEvent;
import io.github.inbox.contract.OrderCreatedEvent;
import io.github.inbox.contract.OrderLine;
import io.github.inbox.loyalty.points.PointsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderEventsListenerUnitTest {

    private static final UUID EVENT_ID = UUID.fromString("0195f0a0-0000-7000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    @Mock
    private PointsService points;

    private PassThroughIdempotentConsumer inbox;
    private OrderEventsListener tested;

    @BeforeEach
    void setUp() {
        inbox = new PassThroughIdempotentConsumer();
        tested = new OrderEventsListener(inbox, points);
    }

    @Test
    @DisplayName("UT onOrderEvent() when order-created arrives should accrue points under the event id of its header")
    void onOrderEvent_whenOrderCreated_shouldAccrueUnderHeaderEventId() {
        // given
        OrderCreatedEvent event = new OrderCreatedEvent(7L, 42L, "jane.doe@example.com",
                List.of(new OrderLine("sku-1", 1, BigDecimal.TEN)), BigDecimal.TEN, "EUR", NOW);

        // when
        tested.onOrderEvent(message(event));

        // then
        verify(points).accrue(event);
        assertThat(inbox.consumedIds()).containsExactly(EVENT_ID);
    }

    @Test
    @DisplayName("UT onOrderEvent() when order-cancelled arrives should revert the points")
    void onOrderEvent_whenOrderCancelled_shouldRevertPoints() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(7L, 42L, null, NOW);

        // when
        tested.onOrderEvent(message(event));

        // then
        verify(points).revert(event);
    }

    @Test
    @DisplayName("UT onOrderEvent() when the payload is of no known event should refuse it untouched")
    void onOrderEvent_whenPayloadIsUnknown_shouldThrowUnsupportedEventException() {
        // when / then
        assertThatThrownBy(() -> tested.onOrderEvent(message("something else")))
                .isInstanceOf(UnsupportedEventException.class)
                .hasMessage("No handling for event payload java.lang.String");
        verifyNoInteractions(points);
    }

    private static Message<?> message(Object payload) {
        return MessageBuilder.withPayload(payload)
                .setHeader("oncebox_event_id", EVENT_ID.toString())
                .build();
    }
}
