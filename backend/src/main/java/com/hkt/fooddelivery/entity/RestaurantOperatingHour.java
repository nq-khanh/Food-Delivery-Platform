package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.time.LocalTime;
import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_operating_hours")
public class RestaurantOperatingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_closed")
    private boolean isClosed = false;

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

    public RestaurantOperatingHour() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ── Builder ────────────────────────────────────────────────

    public static RestaurantOperatingHourBuilder builder() {
        return new RestaurantOperatingHourBuilder();
    }

    public static final class RestaurantOperatingHourBuilder {
        private Restaurant restaurant;
        private Integer dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private boolean isClosed = false;

        public RestaurantOperatingHourBuilder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public RestaurantOperatingHourBuilder dayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; return this; }
        public RestaurantOperatingHourBuilder openTime(LocalTime openTime) { this.openTime = openTime; return this; }
        public RestaurantOperatingHourBuilder closeTime(LocalTime closeTime) { this.closeTime = closeTime; return this; }
        public RestaurantOperatingHourBuilder isClosed(boolean isClosed) { this.isClosed = isClosed; return this; }

        public RestaurantOperatingHour build() {
            RestaurantOperatingHour hour = new RestaurantOperatingHour();
            hour.setRestaurant(restaurant);
            hour.setDayOfWeek(dayOfWeek);
            hour.setOpenTime(openTime);
            hour.setCloseTime(closeTime);
            hour.setClosed(isClosed);
            return hour;
        }
    }
}
