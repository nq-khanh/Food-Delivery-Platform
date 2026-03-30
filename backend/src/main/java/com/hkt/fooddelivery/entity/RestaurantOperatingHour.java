package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

import com.hkt.fooddelivery.entity.enums.DayOfWeek;
import com.hkt.fooddelivery.exception.BusinessException;
import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_operating_hours")
public class RestaurantOperatingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_closed")
    private boolean isClosed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Integer getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public boolean isClosed() { return isClosed; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    protected RestaurantOperatingHour() {}

    RestaurantOperatingHour(
            Restaurant restaurant,
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);

        setOperatingHours(openTime, closeTime);
        this.isClosed = false;
    }

    void setOperatingHours(LocalTime openTime, LocalTime closeTime) {
        Objects.requireNonNull(openTime);
        Objects.requireNonNull(closeTime);

        if (openTime.equals(closeTime)) {
            throw new BusinessException("Open and close time cannot be equal");
        }

        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = false;
    }

    void closeAllDay() {
        this.isClosed = true;
    }

    boolean isOpenAt(LocalTime time) {
        if (this.isClosed) return false;

        if (openTime.isBefore(closeTime)) {
            return !time.isBefore(openTime) && time.isBefore(closeTime);
        }

        return !time.isBefore(openTime) || time.isBefore(closeTime);
    }
}
