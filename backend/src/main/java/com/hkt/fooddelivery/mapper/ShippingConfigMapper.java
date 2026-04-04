package com.hkt.fooddelivery.mapper;

import com.hkt.fooddelivery.dto.ShippingConfigResponse;
import com.hkt.fooddelivery.entity.ShippingConfig;
import org.springframework.stereotype.Component;

@Component
public class ShippingConfigMapper {

    public ShippingConfigResponse toResponse(ShippingConfig c) {
        if (c == null) return null;

        return new ShippingConfigResponse(
                c.getId(),
                c.getConfigName(),
                c.getBaseFee(),
                c.getBaseDistanceKm(),
                c.getFeePerKm(),
                c.getPriority(),
                c.isActive(),
                c.isDefault(),
                c.getActiveFrom(),
                c.getActiveTo()
        );
    }
}
