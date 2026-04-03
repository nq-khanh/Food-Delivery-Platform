package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.ShippingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ShippingConfigRepository extends JpaRepository<ShippingConfig, Integer> {

    List<ShippingConfig> findByIsActiveTrue();

    List<ShippingConfig> findByPriorityAndIsActiveTrue(int priority);

    List<ShippingConfig> findByIsActiveTrueAndActiveFromLessThanEqualAndActiveToGreaterThanEqual(
            Instant from, Instant to
    );

    @Query("""
    SELECT c FROM ShippingConfig c
    WHERE c.isActive = true
      AND (c.activeFrom IS NULL OR c.activeFrom <= :now)
      AND (c.activeTo IS NULL OR c.activeTo > :now)
    ORDER BY c.priority DESC
""")
    List<ShippingConfig> findActiveConfigs(Instant now);
}