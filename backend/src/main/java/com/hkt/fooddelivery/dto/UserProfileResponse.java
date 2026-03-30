package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.enums.Role;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String phone,
        String firstName,
        String lastName,
        String avatarUrl,
        Role role,
        boolean isVerified
) {}