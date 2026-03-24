package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rating_avg", precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

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

    public UUID getId() { return id; }

    public User getOwner() { return owner; }

    public String getName() { return name; }

    public String getAddress() { return address; }

    public String getDescription() { return description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }

    public BigDecimal getRatingAvg() { return ratingAvg; }

    public boolean isActive() { return isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    protected Restaurant() {}

    public Restaurant(User owner, String name, String address, Point location) {
        this.owner = Objects.requireNonNull(owner);
        this.name = Objects.requireNonNull(name);
        this.address = Objects.requireNonNull(address);
        this.location = Objects.requireNonNull(location);
        this.approvalStatus = ApprovalStatus.PENDING;
        this.ratingAvg = BigDecimal.ZERO;
        this.isActive = true;
    }

    public void updateInfo(String name, String address, String description) {
        this.name = Objects.requireNonNull(name);
        this.address = Objects.requireNonNull(address);
        this.description = description;
    }

    private static final BigDecimal MAX_RATING = new BigDecimal("5");

    public void updateRating(BigDecimal newRating) {
        Objects.requireNonNull(newRating);

        if (newRating.compareTo(BigDecimal.ZERO) < 0 ||
                newRating.compareTo(MAX_RATING) > 0) {
            throw new IllegalArgumentException("Invalid rating");
        }

        this.ratingAvg = newRating.setScale(1, RoundingMode.HALF_UP);
    }

    public void approve() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Only pending can be approved");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Only pending can be rejected");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
