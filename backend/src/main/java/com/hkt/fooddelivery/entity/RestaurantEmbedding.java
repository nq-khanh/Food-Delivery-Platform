package com.hkt.fooddelivery.entity;

import java.util.Objects;
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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "embedding", nullable = false)
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    protected RestaurantEmbedding() {
    }

    public RestaurantEmbedding(Restaurant restaurant, float[] embedding) {
        this.restaurant = Objects.requireNonNull(restaurant);
        setEmbedding(embedding);
    }

    public UUID getRestaurantId() { return restaurantId; }

    public Restaurant getRestaurant() { return restaurant; }

    public float[] getEmbedding() { return embedding; }

    private static final int DIMENSION = 1536;

    public void setEmbedding(float[] embedding) {
        Objects.requireNonNull(embedding);
        if (embedding.length != DIMENSION) {
            throw new IllegalArgumentException("Invalid embedding dimension");
        }
        this.embedding = embedding.clone();
    }
}
