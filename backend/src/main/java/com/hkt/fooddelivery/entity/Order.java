package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.OrderPaymentStatus;
import com.hkt.fooddelivery.entity.enums.OrderStatus;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

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
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderReview review;

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

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
        recalculateTotal();
    }

    public void addItem(Product product, int quantity) {
        ensureModifiable();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // 1. Tìm xem sản phẩm đã có trong giỏ hàng chưa
        Optional<OrderItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // 2. Nếu đã có: Cộng dồn số lượng
            existingItem.get().addQuantity(quantity);
        } else {
            // 3. Nếu chưa có: Tạo mới và thêm vào danh sách
            OrderItem newItem = new OrderItem(this, product, quantity, product.getPrice());
            this.items.add(newItem);
        }

        // 4. Luôn tính lại tổng tiền sau khi thay đổi item
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

    public OrderReview review(int resRating, String resComment, Integer shipRating, List<ReviewItemCommand> commands) {
        // 1. Kiểm tra trạng thái chung của Order
        if (this.orderStatus != OrderStatus.COMPLETED) throw new IllegalStateException("Only completed order can be reviewed");
        if (this.review != null) throw new IllegalStateException("Already reviewed");

        // 2. Tạo OrderReview
        OrderReview newReview = new OrderReview(this);
        newReview.reviewRestaurant(resRating, resComment);
        if (shipRating != null)
            newReview.reviewShipper(shipRating);

        // 3. Duyệt danh sách đánh giá món ăn
        if (commands != null) {
            for (ReviewItemCommand data : commands) {
                // Check nghiệp vụ: Món này có thuộc đơn hàng không?
                if (!this.containsProduct(data.product())) {
                    throw new IllegalArgumentException("Sản phẩm không thuộc đơn hàng: " + data.product().getName());
                }

                // Ủy quyền tạo ItemReview cho OrderReview
                newReview.addItemReview(data.product(), data.rating(), data.comment(), data.imageUrl());
            }
        }

        this.review = newReview;
        return newReview;
    }

    private boolean containsProduct(Product product) {
        return this.items.stream()
                .anyMatch(item -> item.getProduct().equals(product));
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