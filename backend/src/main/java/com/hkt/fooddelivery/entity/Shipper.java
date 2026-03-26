package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shippers")
public class Shipper {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "vehicle_info", length = 100)
    private String vehicleInfo;

    @Column(name = "license_plate", unique = true, length = 20)
    private String licensePlate;

    @Column(name = "is_online")
    private boolean isOnline = false;

    @Column(name = "is_busy")
    private boolean isBusy = false;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "rating_avg", precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Shipper() {}

    public Shipper(User user, String licensePlate) {
        this.user = Objects.requireNonNull(user);
        this.id = user.getId();
        this.licensePlate = normalize(licensePlate);
        this.ratingAvg = BigDecimal.ZERO;
        this.isOnline = false;
        this.isBusy = false;
    }


    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getVehicleInfo() { return vehicleInfo; }
    public String getLicensePlate() { return licensePlate; }
    public boolean isOnline() { return isOnline; }
    public boolean isBusy() { return isBusy; }
    public Point getLocation() { return location; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void updateVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = normalizeNullable(vehicleInfo);
    }

    public void updateLicensePlate(String licensePlate) {
        this.licensePlate = normalize(licensePlate);
    }

    public void goOnline() {
        this.isOnline = true;
    }

    public void goOffline() {
        this.isOnline = false;
        this.isBusy = false;
    }

    public void markBusy() {
        if (!isOnline) {
            throw new IllegalStateException("Shipper must be online");
        }
        this.isBusy = true;
    }

    public void markAvailable() {
        this.isBusy = false;
    }

    public void updateLocation(Point location) {
        this.location = Objects.requireNonNull(location);
    }

    private static final BigDecimal MAX_RATING = new BigDecimal("5");

    public void updateRating(BigDecimal rating) {
        Objects.requireNonNull(rating);

        if (rating.compareTo(BigDecimal.ZERO) < 0 ||
                rating.compareTo(MAX_RATING) > 0) {
            throw new IllegalArgumentException("Invalid rating");
        }

        this.ratingAvg = rating.setScale(1, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
        return trimmed;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
