package io.github.inbox.order.events;

import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxEvent;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxManager;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxPropertiesHolder;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Hands an event to the transactional outbox and records it in the {@link PublishedEventEntity} log, both in the
 * caller's transaction: the event exists exactly when the change that caused it is committed.
 * <p>
 * It is built on the outbox's serializer and manager rather than on {@code OutboxPublisher}, because the log needs
 * the id the outbox assigns and {@code OutboxPublisher#publish} does not return it.
 */
@Component
public class EventPublisher {

    private final OutboxPropertiesHolder outboxProperties;
    private final OutboxSerializer serializer;
    private final OutboxManager outbox;
    private final PublishedEventRepository publishedEvents;
    private final Clock clock;

    public EventPublisher(OutboxPropertiesHolder outboxProperties,
                          OutboxSerializer serializer,
                          OutboxManager outbox,
                          PublishedEventRepository publishedEvents,
                          Clock clock) {
        this.outboxProperties = outboxProperties;
        this.serializer = serializer;
        this.outbox = outbox;
        this.publishedEvents = publishedEvents;
        this.clock = clock;
    }

    /**
     * @param eventType one of the event types configured under {@code oncebox.outbox.events}
     * @param payload   the event, serialized to JSON
     * @return the id the event is sent under
     * @throws IllegalArgumentException if no outbox event type of that name is configured
     * @throws org.springframework.transaction.IllegalTransactionStateException if called outside a transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(String eventType, Object payload) {
        if (!outboxProperties.existEventType(eventType)) {
            throw new IllegalArgumentException("Unknown outbox event type '%s'".formatted(eventType));
        }
        OutboxEvent event = serializer.serialize(eventType, payload);
        outbox.save(event);
        publishedEvents.save(new PublishedEventEntity(
                event.getId(),
                event.getEventType(),
                event.getPayloadType(),
                event.getPayload(),
                clock.instant()
        ));
        return event.getId();
    }
}
