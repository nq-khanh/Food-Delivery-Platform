package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "PENDING";

    @Column(name = "rating_avg", precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Restaurant() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ── Builder ────────────────────────────────────────────────

    public static RestaurantBuilder builder() {
        return new RestaurantBuilder();
    }

    public static final class RestaurantBuilder {
        private User owner;
        private String name;
        private String address;
        private String description;
        private String logoUrl;
        private String approvalStatus = "PENDING";
        private BigDecimal ratingAvg = BigDecimal.ZERO;
        private boolean isActive = true;

        public RestaurantBuilder owner(User owner) { this.owner = owner; return this; }
        public RestaurantBuilder name(String name) { this.name = name; return this; }
        public RestaurantBuilder address(String address) { this.address = address; return this; }
        public RestaurantBuilder description(String description) { this.description = description; return this; }
        public RestaurantBuilder logoUrl(String logoUrl) { this.logoUrl = logoUrl; return this; }
        public RestaurantBuilder approvalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; return this; }
        public RestaurantBuilder ratingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; return this; }
        public RestaurantBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }

        public Restaurant build() {
            Restaurant restaurant = new Restaurant();
            restaurant.setOwner(owner);
            restaurant.setName(name);
            restaurant.setAddress(address);
            restaurant.setDescription(description);
            restaurant.setLogoUrl(logoUrl);
            restaurant.setApprovalStatus(approvalStatus);
            restaurant.setRatingAvg(ratingAvg);
            restaurant.setActive(isActive);
            return restaurant;
        }
    }
}
