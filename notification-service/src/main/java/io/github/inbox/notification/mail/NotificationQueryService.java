package io.github.inbox.notification.mail;

import io.github.inbox.notification.api.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationQueryService {

    private final NotificationRepository notifications;

    public NotificationQueryService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /**
     * @return the e-mails sent about the order, oldest first; empty when none was sent
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> findByOrder(Long orderId) {
        return notifications.findByOrderIdOrderBySentAt(orderId).stream()
                .map(notification -> new NotificationResponse(
                        notification.getEventId(),
                        notification.getOrderId(),
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getSentAt()
                ))
                .toList();
    }
}
