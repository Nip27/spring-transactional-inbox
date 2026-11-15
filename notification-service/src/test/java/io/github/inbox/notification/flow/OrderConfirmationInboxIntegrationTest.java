package io.github.inbox.notification.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inbox.contract.EventTypes;
import io.github.inbox.contract.OrderCancelledEvent;
import io.github.inbox.contract.OrderCreatedEvent;
import io.github.inbox.contract.OrderLine;
import io.github.inbox.notification.support.ApplicationIntegrationTest;
import io.github.inbox.notification.support.MailServer;
import io.github.inbox.notification.support.OrderEventSender;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.inbox.notification.support.Await.awaitAtMost;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Order events sent to Kafka as the outbox sends them, consumed through the at-most-once inbox. Every case mails a
 * recipient of its own, since the SMTP server is shared.
 */
class OrderConfirmationInboxIntegrationTest extends ApplicationIntegrationTest {

    private static final AtomicLong IDS = new AtomicLong(1_000);
    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    private final JdbcTemplate jdbc;
    private final OrderEventSender sender;

    OrderConfirmationInboxIntegrationTest(JdbcTemplate jdbc,
                                          KafkaTemplate<String, String> kafkaTemplate,
                                          ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.sender = new OrderEventSender(kafkaTemplate, objectMapper);
    }

    @Test
    @DisplayName("IT order-created when consumed should send one confirmation and record it")
    void orderCreated_whenConsumed_shouldSendOneConfirmation() throws Exception {
        // given
        long orderId = IDS.incrementAndGet();
        String recipient = "customer-%d@example.com".formatted(orderId);
        UUID eventId = UUID.randomUUID();

        // when
        sender.send(eventId, EventTypes.ORDER_CREATED, created(orderId, recipient));

        // then
        awaitAtMost().until(() -> MailServer.receivedBy(recipient).size() == 1);
        MimeMessage mail = MailServer.receivedBy(recipient).getFirst();
        assertThat(mail.getSubject()).isEqualTo("Your order %d is confirmed".formatted(orderId));
        awaitAtMost().until(() -> notificationsOf(eventId) == 1);
        assertThat(isInInbox(eventId)).isTrue();
    }

    @Test
    @DisplayName("IT order-created when delivered twice under one id should send the confirmation once")
    void orderCreated_whenDeliveredTwice_shouldSendOnce() {
        // given
        long orderId = IDS.incrementAndGet();
        String recipient = "customer-%d@example.com".formatted(orderId);
        String marker = "marker-%d@example.com".formatted(orderId);
        String samePartition = recipient;
        UUID eventId = UUID.randomUUID();
        OrderCreatedEvent event = created(orderId, recipient);
        sender.send(eventId, EventTypes.ORDER_CREATED, samePartition, event);
        awaitAtMost().until(() -> MailServer.receivedBy(recipient).size() == 1);

        // when
        sender.send(eventId, EventTypes.ORDER_CREATED, samePartition, event);
        // consumed after the repeat, so once it is mailed the repeat has been handled too
        sender.send(UUID.randomUUID(), EventTypes.ORDER_CREATED, samePartition, created(IDS.incrementAndGet(), marker));

        // then
        awaitAtMost().until(() -> MailServer.receivedBy(marker).size() == 1);
        assertThat(MailServer.receivedBy(recipient)).hasSize(1);
        assertThat(notificationsOf(eventId)).isEqualTo(1);
    }

    @Test
    @DisplayName("IT order-cancelled when consumed should record the event and send nothing")
    void orderCancelled_whenConsumed_shouldSendNothing() {
        // given
        long orderId = IDS.incrementAndGet();
        UUID eventId = UUID.randomUUID();

        // when
        sender.send(eventId, EventTypes.ORDER_CANCELLED, new OrderCancelledEvent(orderId, 42L, null, NOW));

        // then
        awaitAtMost().until(() -> isInInbox(eventId));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE order_id = ?", Long.class, orderId))
                .isZero();
    }

    private long notificationsOf(UUID eventId) {
        return jdbc.queryForObject("SELECT count(*) FROM notifications WHERE event_id = ?", Long.class, eventId);
    }

    private boolean isInInbox(UUID eventId) {
        return jdbc.queryForObject("SELECT count(*) FROM inbox_events WHERE id = ?", Long.class, eventId) == 1;
    }

    private static OrderCreatedEvent created(long orderId, String recipient) {
        return new OrderCreatedEvent(orderId, 42L, recipient,
                List.of(new OrderLine("sku-1", 2, new BigDecimal("10.50"))), new BigDecimal("21.00"), "EUR", NOW);
    }
}
