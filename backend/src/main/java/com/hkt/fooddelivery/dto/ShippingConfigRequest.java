package com.hkt.fooddelivery.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ShippingConfigRequest(
        String configName,
        BigDecimal baseFee,
        BigDecimal baseDistanceKm,
        BigDecimal feePerKm,
        int priority,
        Instant activeFrom,
        Instant activeTo
) {}