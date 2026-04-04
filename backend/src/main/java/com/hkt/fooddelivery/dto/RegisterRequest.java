package com.hkt.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String token,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone,
        // Merchant specific
        String businessName,
        String address,
        LocationDTO location,
        // Shipper specific
        String licensePlate
) {}
