package io.github.inbox.order.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A copy of an event handed to the outbox, kept under the id the outbox assigned to it. The outbox deletes its rows
 * once they are sent; this log keeps them, so an event can be sent again later with its original id.
 */
@Entity
@Table(name = "published_events")
public class PublishedEventEntity {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload_type", nullable = false)
    private String payloadType;

    @Column(nullable = false)
    private String payload;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    protected PublishedEventEntity() {
    }

    public PublishedEventEntity(UUID id, String eventType, String payloadType, String payload, Instant publishedAt) {
        this.id = id;
        this.eventType = eventType;
        this.payloadType = payloadType;
        this.payload = payload;
        this.publishedAt = publishedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
