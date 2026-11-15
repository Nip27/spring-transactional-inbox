package io.github.inbox.loyalty.points;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One change of a customer's balance caused by an order. The ledger is append-only, and a unique key on the order
 * and the kind keeps an order from earning or losing points twice.
 */
@Entity
@Table(name = "points_ledger")
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryKind kind;

    @Column(nullable = false)
    private long points;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntryEntity() {
    }

    public LedgerEntryEntity(Long orderId, Long customerId, LedgerEntryKind kind, long points, Instant createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.kind = kind;
        this.points = points;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LedgerEntryKind getKind() {
        return kind;
    }

    public long getPoints() {
        return points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
