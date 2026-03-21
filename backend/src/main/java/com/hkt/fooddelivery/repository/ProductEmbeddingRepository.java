package com.hkt.fooddelivery.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.ProductEmbedding;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, UUID> {
}
