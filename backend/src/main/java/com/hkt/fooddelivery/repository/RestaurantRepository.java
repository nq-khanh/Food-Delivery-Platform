package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByOwnerId(UUID ownerId);

    @Query("""
        SELECT r FROM Restaurant r
        JOIN FETCH r.owner
        WHERE r.approvalStatus = :status
    """)
    List<Restaurant> findByApprovalStatusWithOwner(ApprovalStatus status);
}
