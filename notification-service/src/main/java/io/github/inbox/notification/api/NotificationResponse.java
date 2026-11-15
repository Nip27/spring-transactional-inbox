package io.github.inbox.notification.api;

import java.time.Instant;
import java.util.UUID;

/**
 * An e-mail the service sent.
 *
 * @param eventId   the order event that caused it
 * @param orderId   the order it is about
 * @param recipient where it was sent
 * @param subject   its subject line
 * @param sentAt    when the mail server accepted it
 */
public record NotificationResponse(UUID eventId, Long orderId, String recipient, String subject, Instant sentAt) {
}
