package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.ShippingConfigRequest;
import com.hkt.fooddelivery.dto.ShippingConfigResponse;
import com.hkt.fooddelivery.service.ShippingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shipping-configs")
@RequiredArgsConstructor
public class ShippingConfigController {

    private final ShippingConfigService service;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ShippingConfigResponse>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.success(service.getCurrent()));
    }

    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<BigDecimal>> calculate(
            @RequestParam BigDecimal distanceKm) {

        return ResponseEntity.ok(ApiResponse.success(
                service.calculateFee(distanceKm)
        ));
    }
}