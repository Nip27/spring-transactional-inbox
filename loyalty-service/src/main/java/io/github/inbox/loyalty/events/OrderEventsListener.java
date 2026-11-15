package io.github.inbox.loyalty.events;

import io.github.dmitriyiliyov.oncebox.core.OnceboxHeadersUtils;
import io.github.dmitriyiliyov.oncebox.core.inbox.IdempotentConsumer;
import io.github.dmitriyiliyov.oncebox.starter.inbox.TransactionalConsumer;
import io.github.inbox.contract.OrderCancelledEvent;
import io.github.inbox.contract.OrderCreatedEvent;
import io.github.inbox.contract.Topics;
import io.github.inbox.loyalty.points.PointsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Turns order events into points, each event at most once in effect.
 * <p>
 * The transactional inbox records the event id in the transaction that changes the points: a failure rolls both back
 * and the redelivered event is handled again, a repeated event that was handled is skipped.
 */
@Component
public class OrderEventsListener {

    private final IdempotentConsumer inbox;
    private final PointsService points;

    public OrderEventsListener(@TransactionalConsumer IdempotentConsumer inbox, PointsService points) {
        this.inbox = inbox;
        this.points = points;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, containerFactory = "inboxKafkaListenerContainerFactory")
    public void onOrderEvent(Message<?> message) {
        inbox.consume(message, OrderEventsListener::eventId, this::handle);
    }

    private static UUID eventId(Message<?> message) {
        try {
            return OnceboxHeadersUtils.extractId(message);
        } catch (IllegalArgumentException e) {
            throw new MalformedEventException("Message without a usable event id: " + e.getMessage(), e);
        }
    }

    private void handle(Message<?> message) {
        switch (message.getPayload()) {
            case OrderCreatedEvent created -> points.accrue(created);
            case OrderCancelledEvent cancelled -> points.revert(cancelled);
            default -> throw new UnsupportedEventException(message.getPayload());
        }
    }
}
