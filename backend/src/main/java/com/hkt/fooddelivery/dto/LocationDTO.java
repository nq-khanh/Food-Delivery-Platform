package com.hkt.fooddelivery.dto;

import jakarta.validation.constraints.NotNull;

public record LocationDTO(
        @NotNull double lat,
        @NotNull double lng
) {}
