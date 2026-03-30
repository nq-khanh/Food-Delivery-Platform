package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import com.hkt.fooddelivery.exception.BusinessException;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "rating_avg", precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Shipper() {}

    public Shipper(User user, String licensePlate) {
        this.user = Objects.requireNonNull(user);
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
    public ApprovalStatus getApprovalStatus() { return approvalStatus;}
    public Point getLocation() { return location; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public int getReviewCount() { return reviewCount; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void updateVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = normalizeNullable(vehicleInfo);
    }

    public void updateLicensePlate(String licensePlate) {
        this.licensePlate = normalize(licensePlate);
    }


    public void goOffline() {
        this.isOnline = false;
        this.isBusy = false;
    }

    public void goOnline() {
        if (this.approvalStatus != ApprovalStatus.APPROVED) {
            throw new BusinessException("Shipper must be approved to go online");
        }
        this.isOnline = true;
    }

    public void markBusy() {
        if (!isOnline || approvalStatus != ApprovalStatus.APPROVED) {
            throw new BusinessException("Shipper must be online and approved");
        }
        this.isBusy = true;
    }

    public void approve() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new BusinessException("Only pending can be approved");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new BusinessException("Only pending can be rejected");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public void markAvailable() {
        this.isBusy = false;
    }

    public void updateLocation(Point location) {
        this.location = Objects.requireNonNull(location);
    }

    public void updateRating(int newRating) {
        BigDecimal totalScore = this.ratingAvg
                .multiply(BigDecimal.valueOf(this.reviewCount))
                .add(BigDecimal.valueOf(newRating));

        this.reviewCount++;
        this.ratingAvg = totalScore.divide(BigDecimal.valueOf(this.reviewCount), 1, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException("Value cannot be blank");
        }
        return trimmed;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
