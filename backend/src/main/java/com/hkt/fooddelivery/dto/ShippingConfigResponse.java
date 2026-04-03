package com.hkt.fooddelivery.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ShippingConfigResponse(
        Integer id,
        String configName,
        BigDecimal baseFee,
        BigDecimal baseDistanceKm,
        BigDecimal feePerKm,
        int priority,
        boolean isActive,
        boolean isDefault,
        Instant activeFrom,
        Instant activeTo
) {}