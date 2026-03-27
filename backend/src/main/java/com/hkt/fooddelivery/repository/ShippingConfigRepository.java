package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.ShippingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShippingConfigRepository extends JpaRepository<ShippingConfig, UUID> {
}
