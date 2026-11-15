package io.github.inbox.order.events;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PublishedEventRepository extends JpaRepository<PublishedEventEntity, UUID> {
}
