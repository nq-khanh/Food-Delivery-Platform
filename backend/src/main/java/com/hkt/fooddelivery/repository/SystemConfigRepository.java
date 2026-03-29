package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {
}
