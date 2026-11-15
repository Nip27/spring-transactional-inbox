package io.github.inbox.notification.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByOrderIdOrderBySentAt(Long orderId);

    boolean existsByEventId(UUID eventId);
}
