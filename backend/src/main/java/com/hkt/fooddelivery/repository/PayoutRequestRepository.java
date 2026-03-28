package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.PayoutRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, UUID> {
}
