package com.hkt.fooddelivery.dto;

import com.hkt.fooddelivery.entity.enums.DiscountType;
import com.hkt.fooddelivery.entity.enums.TargetType;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherRequest(
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        TargetType targetType,
        BigDecimal minOrderValue,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Instant expiryDate
) {}