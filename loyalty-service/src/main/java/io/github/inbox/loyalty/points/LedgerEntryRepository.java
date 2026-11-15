package io.github.inbox.loyalty.points;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {

    Optional<LedgerEntryEntity> findByOrderIdAndKind(Long orderId, LedgerEntryKind kind);
}
