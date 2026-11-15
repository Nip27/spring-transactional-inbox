package io.github.inbox.loyalty.points;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * The current points balance of a customer, the sum of the customer's ledger entries.
 * <p>
 * Two events of one customer handled at once both change this row; the {@link Version} turns the second commit into
 * an optimistic locking failure instead of a lost update, and the event is handled again on fresh state.
 */
@Entity
@Table(name = "customer_points")
public class CustomerPointsEntity {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private long balance;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected CustomerPointsEntity() {
    }

    public CustomerPointsEntity(Long customerId, Instant createdAt) {
        this.customerId = customerId;
        this.balance = 0;
        this.updatedAt = createdAt;
    }

    public void add(long points, Instant at) {
        this.balance += points;
        this.updatedAt = at;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public long getBalance() {
        return balance;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
