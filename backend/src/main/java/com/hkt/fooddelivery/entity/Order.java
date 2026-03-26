package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private OrderPaymentStatus orderPaymentStatus;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_location", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point deliveryLocation;

    private Instant placedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        if (this.orderCode == null) {
            this.orderCode = "ORD-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public User getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Shipper getShipper() { return shipper; }
    public Voucher getVoucher() { return voucher; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public OrderPaymentStatus getOrderPaymentStatus() { return orderPaymentStatus; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Point getDeliveryLocation() { return deliveryLocation; }
    public Instant getPlacedAt() { return placedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public void setNotes(String notes) { this.notes = notes; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
        recalculateTotal();
    }

    protected Order() {
    }

    public Order(User customer, Restaurant restaurant, String deliveryAddress, Point deliveryLocation) {

        this.customer = Objects.requireNonNull(customer);
        this.restaurant = Objects.requireNonNull(restaurant);
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress);
        this.deliveryLocation = Objects.requireNonNull(deliveryLocation);

        this.orderStatus = OrderStatus.PENDING;
        this.orderPaymentStatus = OrderPaymentStatus.UNPAID;
        this.subtotal = BigDecimal.ZERO;
        this.shippingFee = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addItem(Product product, int quantity) {
        ensureModifiable();

        if (quantity <= 0) throw new IllegalArgumentException();

        OrderItem item = new OrderItem(this, product, quantity, product.getPrice());
        this.items.add(item);

        recalculateSubtotal();
    }

    public void applyVoucher(Voucher voucher) {
        ensureModifiable();

        if (!voucher.canUse(this.subtotal)) {
            throw new IllegalStateException("Voucher invalid");
        }

        this.voucher = voucher;
        this.discountAmount = voucher.calculateDiscount(subtotal);

        recalculateTotal();
    }

    public void confirm(User actor) {
        if (items.isEmpty()) {
            throw new IllegalStateException("Order must have items");
        }

        if (orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException("Invalid state");
        }

        this.orderStatus = OrderStatus.CONFIRMED;
        this.placedAt = Instant.now();

        if (voucher != null) {
            voucher.markUsed();
        }

        addHistory(OrderStatus.CONFIRMED, actor, null);
    }

    public void assignShipper(Shipper shipper) {
        if (orderStatus != OrderStatus.CONFIRMED) {
            throw new IllegalStateException();
        }
        this.shipper = shipper;
    }

    public void startShipping(User actor) {
        if (orderStatus != OrderStatus.CONFIRMED) {
            throw new IllegalStateException();
        }

        this.orderStatus = OrderStatus.SHIPPING;

        addHistory(OrderStatus.SHIPPING, actor, null);
    }

    public void complete(User actor) {
        if (orderStatus != OrderStatus.SHIPPING) {
            throw new IllegalStateException();
        }

        this.orderStatus = OrderStatus.COMPLETED;
        this.completedAt = Instant.now();

        addHistory(OrderStatus.COMPLETED, actor, null);
    }

    public void cancel(User actor, String reason) {
        if (orderStatus == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancel reason required");
        }

        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();

        addHistory(OrderStatus.CANCELLED, actor, reason);
    }

    public void markPaid() {
        if (orderPaymentStatus != OrderPaymentStatus.UNPAID) {
            throw new IllegalStateException();
        }
        this.orderPaymentStatus = OrderPaymentStatus.PAID;
    }

    private void recalculateSubtotal() {
        this.subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = subtotal
                .add(shippingFee)
                .subtract(discountAmount);
    }

    private void ensureModifiable() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be modified");
        }
    }

    private void addHistory(OrderStatus status, User actor, String reason) {
        OrderStatusHistory history = new OrderStatusHistory(this, status, actor, reason);
        statusHistories.add(history);
    }
}