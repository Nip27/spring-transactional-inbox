package io.github.inbox.loyalty.points;

import io.github.inbox.contract.OrderCancelledEvent;
import io.github.inbox.contract.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Keeps the ledger and the balances in step with order events.
 * <p>
 * Events of one order arrive in no guaranteed order, so a cancellation that comes first is recorded as a reversal of
 * zero points, and an accrual that comes after it is not made: the balance ends the same whichever event was first.
 * A repeated event is not this class's concern - the inbox in front of it does not let one through.
 */
@Service
public class PointsService {

    private static final Logger log = LoggerFactory.getLogger(PointsService.class);

    private final CustomerPointsRepository balances;
    private final LedgerEntryRepository ledger;
    private final PointsPolicy policy;
    private final Clock clock;

    public PointsService(CustomerPointsRepository balances,
                         LedgerEntryRepository ledger,
                         PointsPolicy policy,
                         Clock clock) {
        this.balances = balances;
        this.ledger = ledger;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional
    public void accrue(OrderCreatedEvent event) {
        if (ledger.findByOrderIdAndKind(event.orderId(), LedgerEntryKind.REVERSAL).isPresent()) {
            log.info("Order {} was cancelled before its points were accrued, nothing to accrue", event.orderId());
            return;
        }
        long points = policy.pointsFor(event.totalAmount());
        record(event.orderId(), event.customerId(), LedgerEntryKind.ACCRUAL, points);
    }

    @Transactional
    public void revert(OrderCancelledEvent event) {
        long points = ledger.findByOrderIdAndKind(event.orderId(), LedgerEntryKind.ACCRUAL)
                .map(accrual -> -accrual.getPoints())
                .orElse(0L);
        record(event.orderId(), event.customerId(), LedgerEntryKind.REVERSAL, points);
    }

    private void record(Long orderId, Long customerId, LedgerEntryKind kind, long points) {
        Instant now = clock.instant();
        ledger.save(new LedgerEntryEntity(orderId, customerId, kind, points, now));
        CustomerPointsEntity balance = balances.findById(customerId)
                .orElseGet(() -> new CustomerPointsEntity(customerId, now));
        balance.add(points, now);
        balances.save(balance);
        log.debug("Order {} {}: {} points for customer {}", orderId, kind, points, customerId);
    }
}
