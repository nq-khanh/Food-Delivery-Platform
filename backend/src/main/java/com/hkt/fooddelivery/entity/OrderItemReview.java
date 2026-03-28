package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_item_reviews")
public class OrderItemReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_review_id", nullable = false)
    private OrderReview orderReview;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_visible")
    private boolean isVisible = true;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    protected OrderItemReview() {}

    OrderItemReview(OrderReview orderReview, Product product, int rating) {
        this.orderReview = Objects.requireNonNull(orderReview);
        this.product = Objects.requireNonNull(product);
        this.rating = validateRating(rating);
    }


    public UUID getId() { return id; }
    public OrderReview getOrderReview() { return orderReview; }
    public Product getProduct() { return product; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isVisible() { return  isVisible; }


    void updateContent(String comment, String imageUrl) {
        this.comment = comment;
        this.imageUrl = imageUrl;
    }

    void changeRating(int rating) {
        this.rating = validateRating(rating);
    }

    void hide() {
        this.isVisible = false;
    }

    void show() {
        this.isVisible = true;
    }

    private int validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return rating;
    }
}