package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import org.locationtech.jts.geom.Point;

public record RegisterRequest(
        @NotBlank String token,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone,
        Role role,
        // Merchant specific
        String businessName,
        String address,
        LocationDTO location,
        // Shipper specific
        String licensePlate
) {}
