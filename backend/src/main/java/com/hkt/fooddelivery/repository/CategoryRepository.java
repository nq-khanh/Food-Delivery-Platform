package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByRestaurantIdOrderByDisplayOrderAsc(UUID restaurantId);

    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);
}
