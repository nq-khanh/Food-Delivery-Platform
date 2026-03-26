package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected OrderStatusHistory() {}

    OrderStatusHistory(Order order, OrderStatus status, User changedBy, String reason) {
        this.order = Objects.requireNonNull(order);
        this.status = Objects.requireNonNull(status);
        this.changedBy = changedBy;
        this.reason = reason;
        this.changedAt = Instant.now();
    }


    public Integer getId() { return id; }
    public Order getOrder() { return order; }
    public OrderStatus getStatus() { return status; }
    public User getChangedBy() { return changedBy; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}
