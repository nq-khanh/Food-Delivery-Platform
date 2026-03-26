package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "shipping_configs")
public class ShippingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "config_name", length = 100)
    private String configName;

    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "base_distance_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal baseDistanceKm;

    @Column(name = "fee_per_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal feePerKm;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "active_from")
    private Instant activeFrom;

    @Column(name = "active_to")
    private Instant activeTo;

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

    protected ShippingConfig() {}

    public ShippingConfig(String configName,
                          BigDecimal baseFee,
                          BigDecimal baseDistanceKm,
                          BigDecimal feePerKm) {

        this.configName = normalizeNullable(configName);
        this.baseFee = validateMoney(baseFee);
        this.baseDistanceKm = validatePositive(baseDistanceKm);
        this.feePerKm = validateMoney(feePerKm);

        this.isActive = true;
        this.activeFrom = Instant.now();
    }


    public Integer getId() { return id; }
    public String getConfigName() { return configName; }
    public BigDecimal getBaseFee() { return baseFee; }
    public BigDecimal getBaseDistanceKm() { return baseDistanceKm; }
    public BigDecimal getFeePerKm() { return feePerKm; }
    public boolean isActive() { return isActive; }
    public Instant getActiveFrom() { return activeFrom; }
    public Instant getActiveTo() { return activeTo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void updatePricing(BigDecimal baseFee, BigDecimal baseDistanceKm, BigDecimal feePerKm) {

        this.baseFee = validateMoney(baseFee);
        this.baseDistanceKm = validatePositive(baseDistanceKm);
        this.feePerKm = validateMoney(feePerKm);
    }

    public void rename(String name) {
        this.configName = normalizeNullable(name);
    }

    public void activate() {
        if (isActive) return;
        this.isActive = true;
        this.activeFrom = Instant.now();
        this.activeTo = null;
    }

    public void deactivate() {
        if (!isActive) return;
        this.isActive = false;
        this.activeTo = Instant.now();
    }

    public boolean isEffectiveAt(Instant time) {
        if (!isActive) return false;

        boolean afterStart = activeFrom == null || !time.isBefore(activeFrom);
        boolean beforeEnd = activeTo == null || time.isBefore(activeTo);

        return afterStart && beforeEnd;
    }


    private BigDecimal validateMoney(BigDecimal value) {
        Objects.requireNonNull(value);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money must be >= 0");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal validatePositive(BigDecimal value) {
        Objects.requireNonNull(value);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Must be > 0");
        }
        return value;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
