package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.enums.DiscountType;
import com.hkt.fooddelivery.entity.enums.TargetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VoucherResponse(
        UUID id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        TargetType targetType,
        BigDecimal minOrderValue,
        BigDecimal maxDiscountAmount,
        int usageLimit,
        int usedCount,
        Instant expiryDate,
        boolean isActive,
        Instant createdAt
) {}