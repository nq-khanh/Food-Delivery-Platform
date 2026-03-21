package com.hkt.fooddelivery.entity;

import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "restaurant_embeddings")
public class RestaurantEmbedding {

    @Id
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    public RestaurantEmbedding() {
    }

    public RestaurantEmbedding(Restaurant restaurant, float[] embedding) {
        this.restaurant = restaurant;
        this.restaurantId = restaurant.getId();
        this.embedding = embedding;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getRestaurantId() { return restaurantId; }
    public void setRestaurantId(UUID restaurantId) { this.restaurantId = restaurantId; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.restaurantId = restaurant.getId();
    }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
