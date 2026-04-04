package com.hkt.fooddelivery.mapper;

import com.hkt.fooddelivery.dto.VoucherResponse;
import com.hkt.fooddelivery.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {

    public VoucherResponse toResponse(Voucher v) {
        if (v == null) return null;

        return new VoucherResponse(
                v.getId(),
                v.getCode(),
                v.getDiscountType(),
                v.getDiscountValue(),
                v.getTargetType(),
                v.getMinOrderValue(),
                v.getMaxDiscountAmount(),
                v.getUsageLimit(),
                v.getUsedCount(),
                v.getExpiryDate(),
                v.isActive(),
                v.getCreatedAt()
        );
    }
}