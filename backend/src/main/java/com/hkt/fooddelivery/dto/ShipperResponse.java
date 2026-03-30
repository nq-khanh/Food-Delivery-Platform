package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.enums.ApprovalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShipperResponse(
        UUID id,
        String vehicleInfo,
        String licensePlate,
        boolean isOnline,
        boolean isBusy,
        ApprovalStatus approvalStatus,
        Double lat,
        Double lng,
        BigDecimal ratingAvg,
        int reviewCount,
        Instant updatedAt,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String username,
            String email,
            String fullName,
            String phone
    ) {}
}