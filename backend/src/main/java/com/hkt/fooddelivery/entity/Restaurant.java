package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import com.hkt.fooddelivery.entity.enums.DayOfWeek;
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

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private RestaurantEmbedding embedding;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantOperatingHour> operatingHours = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Restaurant() {}

    public Restaurant(User owner, String name, String address, Point location) {
        this.owner = Objects.requireNonNull(owner);
        this.name = requireNonBlank(name);
        this.address = requireNonBlank(address);
        this.location = Objects.requireNonNull(location);

        this.approvalStatus = ApprovalStatus.PENDING;
        this.ratingAvg = BigDecimal.ZERO.setScale(1);
        this.isActive = false;
    }

    public UUID getId() { return id; }
    public User getOwner() { return owner; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getLogoUrl() { return logoUrl; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public int getReviewCount() { return reviewCount; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void updateInfo(String name, String address, String description) {
        this.name = requireNonBlank(name);
        this.address = requireNonBlank(address);
        this.description = description;
    }

    public void updateLocation(Point location) {
        this.location = Objects.requireNonNull(location);
    }

    public void updateLogo(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void approve() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Only pending can be approved");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.isActive = true;
    }

    public void reject() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Only pending can be rejected");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.isActive = false;
    }

    public void deactivate() {
        if (this.approvalStatus != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved restaurant can be deactivated");
        }
        this.isActive = false;
    }

    public void activate() {
        if (this.approvalStatus != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved restaurant can be activated");
        }
        this.isActive = true;
    }

    public void updateRating(int newRating) {
        BigDecimal totalScore = this.ratingAvg
                .multiply(BigDecimal.valueOf(this.reviewCount))
                .add(BigDecimal.valueOf(newRating));

        this.reviewCount++;
        this.ratingAvg = totalScore.divide(BigDecimal.valueOf(this.reviewCount), 1, RoundingMode.HALF_UP);
    }

    public void setAIEmbedding(float[] vector) {
        this.embedding = new RestaurantEmbedding(this, vector);
    }

    public void addOperatingHour(DayOfWeek day, LocalTime open, LocalTime close) {
        RestaurantOperatingHour hour = new RestaurantOperatingHour(this, day, open, close);
        this.operatingHours.add(hour);
    }

    public boolean isOpenNow(Instant now) {
        if (!this.isActive || this.approvalStatus != ApprovalStatus.APPROVED) {
            return false;
        }

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime zdt = now.atZone(zoneId);

        DayOfWeek currentDay = DayOfWeek.valueOf(zdt.getDayOfWeek().name());
        LocalTime currentTime = zdt.toLocalTime();

        return this.operatingHours.stream()
                .filter(oh -> oh.getDayOfWeek() == currentDay)
                .anyMatch(oh -> oh.isOpenAt(currentTime));
    }

    public boolean isOpenNow() {
        return isOpenNow(Instant.now());
    }

    private String requireNonBlank(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
        return trimmed;
    }
}
