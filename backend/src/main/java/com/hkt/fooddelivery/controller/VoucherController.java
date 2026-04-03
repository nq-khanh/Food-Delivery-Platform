package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.VoucherRequest;
import com.hkt.fooddelivery.dto.VoucherResponse;
import com.hkt.fooddelivery.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService service;

    // 🔥 Validate voucher trước khi apply
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<VoucherResponse>> validate(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {

        return ResponseEntity.ok(ApiResponse.success(
                service.validateVoucher(code, amount)
        ));
    }

    // 🔥 Apply voucher (real usage)
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<BigDecimal>> apply(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {

        return ResponseEntity.ok(ApiResponse.success(
                service.applyDiscount(code, amount)
        ));
    }
}