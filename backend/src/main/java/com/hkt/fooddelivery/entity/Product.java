package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_available")
    private boolean isAvailable = true;

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

    public Restaurant getRestaurant() { return restaurant; }

    public Category getCategory() { return category; }

    public String getName() { return name; }

    public BigDecimal getPrice() { return price; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }

    public boolean isAvailable() { return isAvailable; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    protected Product() {}

    public Product(Restaurant restaurant, String name, BigDecimal price) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.name = normalizeName(name);
        this.price = validatePrice(price);
        this.isAvailable = true;
    }

    public Product(Restaurant restaurant, Category category, String name, BigDecimal price) {
        this(restaurant, name, price);
        changeCategory(category);
    }

    public void rename(String name) {
        this.name = normalizeName(name);
    }

    public void changePrice(BigDecimal price) {
        this.price = validatePrice(price);
    }

    public void changeCategory(Category category) {
        if (category != null && !category.getRestaurant().equals(this.restaurant)) {
            throw new IllegalArgumentException("Category must belong to same restaurant");
        }
        this.category = category;
    }

    public void updateInfo(String name, String description, String imageUrl) {
        this.name = normalizeName(name);
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void activate() {
        this.isAvailable = true;
    }

    public void deactivate() {
        this.isAvailable = false;
    }

    private String normalizeName(String name) {
        Objects.requireNonNull(name);
        String value = name.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        return value;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        Objects.requireNonNull(price);
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
