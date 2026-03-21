package com.hkt.fooddelivery.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

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

    public Category() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ── Builder ────────────────────────────────────────────────

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static final class CategoryBuilder {
        private Restaurant restaurant;
        private String name;
        private Integer displayOrder = 0;

        public CategoryBuilder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder displayOrder(Integer displayOrder) { this.displayOrder = displayOrder; return this; }

        public Category build() {
            Category category = new Category();
            category.setRestaurant(restaurant);
            category.setName(name);
            category.setDisplayOrder(displayOrder);
            return category;
        }
    }
}
