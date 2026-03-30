package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.listener.OrderReviewListener;
import com.hkt.fooddelivery.exception.BusinessException;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_reviews")
@EntityListeners(OrderReviewListener.class)
public class OrderReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "restaurant_rating")
    private Integer restaurantRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    @Column(name = "shipper_rating")
    private Integer shipperRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_visible")
    private boolean isVisible = true;

    @Column(name = "restaurant_reply", columnDefinition = "TEXT")
    private String restaurantReply;

    @Column(name = "replied_at")
    private Instant repliedAt;

    @OneToMany(mappedBy = "orderReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemReview> itemReviews = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    protected OrderReview(){

    }

    OrderReview(Order order) {
        this.order = Objects.requireNonNull(order);
        this.restaurant = order.getRestaurant();
    }


    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Restaurant getRestaurant() { return restaurant; }
    public Integer getRestaurantRating() { return restaurantRating; }
    public String getComment() { return comment; }
    public Shipper getShipper() { return shipper; }
    public Integer getShipperRating() { return shipperRating; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isVisible() { return  isVisible; }
    public String getRestaurantReply() { return restaurantReply; }
    public Instant getRepliedAt() { return repliedAt; }


    public List<OrderItemReview> getItemReviews() {
        return List.copyOf(itemReviews);
    }


    void reviewRestaurant(int rating, String comment) {
        if (this.restaurantRating != null) {
            throw new BusinessException("Already reviewed");
        }

        validateRating(rating);

        this.restaurantRating = rating;
        this.comment = comment;
    }

    void reviewShipper(int rating) {
        if (this.shipperRating != null) {
            throw new BusinessException("Shipper already reviewed");
        }

        Shipper shipper = order.getShipper();

        if (shipper == null) {
            throw new BusinessException("Order has no shipper");
        }

        validateRating(rating);

        this.shipper = shipper;
        this.shipperRating = rating;
    }

    void addItemReview(Product product, int rating, String comment, String imageUrl) {

        boolean exists = itemReviews.stream()
                .anyMatch(r -> r.getProduct().equals(product));

        if (exists) {
            throw new BusinessException("Product already reviewed");
        }

        OrderItemReview itemReview = new OrderItemReview(this, product, rating);
        itemReview.updateContent(comment, imageUrl);

        this.itemReviews.add(itemReview);
    }

    void replyByRestaurant(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("Reply content cannot be blank");
        }
        this.restaurantReply = content;
        this.repliedAt = Instant.now();
    }

    void hide() {
        this.isVisible = false;
    }

    void show() {
        this.isVisible = true;
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }
    }
}