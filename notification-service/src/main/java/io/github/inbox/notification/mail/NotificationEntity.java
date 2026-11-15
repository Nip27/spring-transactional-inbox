package io.github.inbox.notification.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * An e-mail that was handed to the mail server, with the event that caused it.
 */
@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(UUID eventId, Long orderId, String recipient, String subject, Instant sentAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.recipient = recipient;
        this.subject = subject;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
