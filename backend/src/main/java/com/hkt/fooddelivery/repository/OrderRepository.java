package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
