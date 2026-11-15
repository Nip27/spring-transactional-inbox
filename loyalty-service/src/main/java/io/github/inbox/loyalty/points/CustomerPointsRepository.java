package io.github.inbox.loyalty.points;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPointsRepository extends JpaRepository<CustomerPointsEntity, Long> {
}
