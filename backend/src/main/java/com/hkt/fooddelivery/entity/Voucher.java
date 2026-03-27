package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.DiscountType;
import com.hkt.fooddelivery.entity.enums.TargetType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private TargetType targetType = TargetType.ORDER;

    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private int usageLimit = 1;

    @Column(name = "used_count")
    private int usedCount = 0;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Voucher() {}

    public Voucher(String code, DiscountType discountType, BigDecimal discountValue, Instant expiryDate) {
        this.code = normalize(code);
        this.discountType = Objects.requireNonNull(discountType);
        this.discountValue = validateDiscount(discountValue);
        this.expiryDate = Objects.requireNonNull(expiryDate);

        this.targetType = TargetType.ORDER;
        this.minOrderValue = BigDecimal.ZERO;
        this.usageLimit = 1;
        this.usedCount = 0;
        this.isActive = true;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public TargetType getTargetType() { return targetType; }
    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public int getUsageLimit() { return usageLimit; }
    public int getUsedCount() { return usedCount; }
    public Instant getExpiryDate() { return expiryDate; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    public boolean canUse(BigDecimal orderAmount) {
        Objects.requireNonNull(orderAmount);

        if (!isActive) return false;
        if (isExpired()) return false;
        if (usedCount >= usageLimit) return false;
        if (orderAmount.compareTo(minOrderValue) < 0) return false;

        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!canUse(orderAmount)) {
            throw new IllegalStateException("Voucher cannot be used");
        }

        BigDecimal discount;

        if (discountType == DiscountType.FIXED) {
            discount = discountValue;
        } else {
            discount = orderAmount
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (maxDiscountAmount != null &&
                    discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        }

        return discount;
    }

    public void markUsed() {
        if (usedCount >= usageLimit) {
            throw new IllegalStateException("Voucher usage exceeded");
        }
        this.usedCount++;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }


    private String normalize(String code) {
        Objects.requireNonNull(code);
        String value = code.trim().toUpperCase();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Code cannot be blank");
        }
        return value;
    }

    private BigDecimal validateDiscount(BigDecimal value) {
        Objects.requireNonNull(value);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount must be > 0");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}