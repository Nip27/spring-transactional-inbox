package io.github.inbox.order.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An order with its lines. The total is computed from the lines when the order is created and is not changed
 * afterwards; a cancellation keeps it, so what was charged stays readable.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Version
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(Long customerId,
                       String customerEmail,
                       String currency,
                       List<OrderLineEntity> lines,
                       Instant createdAt) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.currency = currency;
        this.createdAt = createdAt;
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
        for (OrderLineEntity line : lines) {
            line.attachTo(this);
            this.lines.add(line);
            this.totalAmount = this.totalAmount.add(line.amount());
        }
    }

    /**
     * Moves the order to {@link OrderStatus#CANCELLED}.
     *
     * @param reason      why the order is cancelled, or {@code null} when none was given
     * @param cancelledAt when the order is cancelled
     * @throws IllegalStateException if the order is already cancelled
     */
    public void cancel(String reason, Instant cancelledAt) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order %d is already cancelled".formatted(id));
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = cancelledAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public long getVersion() {
        return version;
    }

    public List<OrderLineEntity> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
