package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByRestaurantId(UUID restaurantId);

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByRestaurantIdAndIsAvailableTrue(UUID restaurantId);
}
