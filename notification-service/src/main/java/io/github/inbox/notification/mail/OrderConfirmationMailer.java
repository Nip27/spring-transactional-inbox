package io.github.inbox.notification.mail;

import io.github.inbox.contract.OrderCreatedEvent;
import io.github.inbox.contract.OrderLine;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/**
 * Sends the confirmation of a placed order and records that it was sent.
 * <p>
 * The record is written after the mail server accepted the message: a mail that failed is not recorded, and a mail
 * that was sent stays sent even if the record then fails.
 */
@Component
public class OrderConfirmationMailer {

    private final JavaMailSender mailSender;
    private final NotificationRepository notifications;
    private final MailProperties properties;
    private final Clock clock;

    public OrderConfirmationMailer(JavaMailSender mailSender,
                                   NotificationRepository notifications,
                                   MailProperties properties,
                                   Clock clock) {
        this.mailSender = mailSender;
        this.notifications = notifications;
        this.properties = properties;
        this.clock = clock;
    }

    public void sendConfirmation(UUID eventId, OrderCreatedEvent order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(order.customerEmail());
        message.setSubject(subject(order));
        message.setText(body(order));
        mailSender.send(message);
        notifications.save(new NotificationEntity(eventId, order.orderId(), order.customerEmail(), message.getSubject(),
                clock.instant()));
    }

    static String subject(OrderCreatedEvent order) {
        return "Your order %d is confirmed".formatted(order.orderId());
    }

    private static String body(OrderCreatedEvent order) {
        StringBuilder text = new StringBuilder("Thank you for your order %d.%n%n".formatted(order.orderId()));
        for (OrderLine line : order.lines()) {
            text.append("%d x %s at %s %s%n".formatted(
                    line.quantity(), line.productId(), line.unitPrice().toPlainString(), order.currency()));
        }
        text.append("%nTotal: %s %s%n".formatted(order.totalAmount().toPlainString(), order.currency()));
        return text.toString();
    }
}
