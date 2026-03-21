package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByOwnerId(UUID ownerId);

    List<Restaurant> findByOwnerIdAndIsActiveTrue(UUID ownerId);

    List<Restaurant> findByApprovalStatus(String approvalStatus);

    List<Restaurant> findByIsActiveTrue();

    boolean existsByOwnerIdAndName(UUID ownerId, String name);
}
