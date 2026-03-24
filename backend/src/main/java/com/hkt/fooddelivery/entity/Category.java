package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

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

    public Integer getId() { return id; }

    public Restaurant getRestaurant() { return restaurant; }

    public String getName() { return name; }

    public Integer getDisplayOrder() { return displayOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    protected Category() {}

    public Category(Restaurant restaurant, String name) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.name = Objects.requireNonNull(name);
        this.displayOrder = 0;
    }

    public void rename(String name) {
        this.name = normalize(name);
    }

    public void changeDisplayOrder(int order) {
        if (order < 0) throw new IllegalArgumentException();
        this.displayOrder = order;
    }

    private String normalize(String name) {
        Objects.requireNonNull(name);
        String value = name.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        return value;
    }
}
