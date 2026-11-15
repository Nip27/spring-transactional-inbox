package io.github.inbox.notification.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByOrderIdOrderBySentAt(Long orderId);
}
