package com.hkt.fooddelivery.entity;

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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    public ProductEmbedding() {
    }

    public ProductEmbedding(Product product, float[] embedding) {
        this.product = product;
        this.productId = product.getId();
        this.embedding = embedding;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getId();
    }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
