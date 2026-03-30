package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String address,
        Double lat,
        Double lng,
        String description,
        String logoUrl,
        ApprovalStatus approvalStatus,
        BigDecimal ratingAvg,
        int reviewCount,
        boolean isActive,
        Instant createdAt,
        OwnerInfo owner
) {
    public record OwnerInfo(
            UUID id,
            String username,
            String email,
            String fullName,
            String phone
    ) {}

    // Static method để convert từ Entity sang DTO
    public static RestaurantResponse fromEntity(Restaurant res) {
        Double latitude = (res.getLocation() != null) ? res.getLocation().getY() : null;
        Double longitude = (res.getLocation() != null) ? res.getLocation().getX() : null;

        OwnerInfo ownerInfo = null;
        if (res.getOwner() != null) {
            ownerInfo = new OwnerInfo(
                    res.getOwner().getId(),
                    res.getOwner().getUsername(),
                    res.getOwner().getEmail(),
                    res.getOwner().getFirstName() + " " + res.getOwner().getLastName(),
                    res.getOwner().getPhone()
            );
        }

        return new RestaurantResponse(
                res.getId(),
                res.getName(),
                res.getAddress(),
                latitude,
                longitude,
                res.getDescription(),
                res.getLogoUrl(),
                res.getApprovalStatus(),
                res.getRatingAvg(),
                res.getReviewCount(),
                res.isActive(),
                res.getCreatedAt(),
                ownerInfo
        );
    }
}