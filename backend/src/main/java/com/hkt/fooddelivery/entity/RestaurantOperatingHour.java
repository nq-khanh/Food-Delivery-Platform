package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

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

    public RestaurantOperatingHour() {
    }


    public Integer getId() { return id; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }

    public LocalTime getOpenTime() { return openTime; }

    public LocalTime getCloseTime() { return closeTime; }

    public boolean isClosed() { return isClosed; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void open(LocalTime openTime, LocalTime closeTime) {
        Objects.requireNonNull(openTime);
        Objects.requireNonNull(closeTime);

        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Invalid time range");
        }

        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = false;
    }

    public void close() {
        this.isClosed = true;
        this.openTime = null;
        this.closeTime = null;
    }

    public boolean isOpenAt(LocalTime time) {
        if (isClosed) return false;
        return !time.isBefore(openTime) && time.isBefore(closeTime);
    }
}
