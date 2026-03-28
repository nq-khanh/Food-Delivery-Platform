package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.ShippingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ShippingConfigRepository extends JpaRepository<ShippingConfig, UUID> {
    // Tìm cấu hình đang active và thời gian hiện tại nằm trong khoảng cho phép
    @Query("SELECT s FROM ShippingConfig s " +
            "WHERE s.isActive = true " +
            "AND (s.activeFrom IS NULL OR s.activeFrom <= :now) " +
            "AND (s.activeTo IS NULL OR s.activeTo > :now) " +
            "ORDER BY s.createdAt DESC")
    Optional<ShippingConfig> findCurrentConfig(@Param("now") Instant now);
}
