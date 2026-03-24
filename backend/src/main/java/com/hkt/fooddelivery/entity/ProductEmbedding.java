package com.hkt.fooddelivery.entity;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_embeddings")
public class ProductEmbedding {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "embedding", nullable = false)
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    protected ProductEmbedding() {
    }

    public ProductEmbedding(Product product, float[] embedding) {
        this.product = Objects.requireNonNull(product);
        setEmbedding(embedding);
    }

    public UUID getProductId() { return productId; }

    public Product getProduct() { return product; }

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
