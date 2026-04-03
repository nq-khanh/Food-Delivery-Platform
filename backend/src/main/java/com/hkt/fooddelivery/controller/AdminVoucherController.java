package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.VoucherRequest;
import com.hkt.fooddelivery.dto.VoucherResponse;
import com.hkt.fooddelivery.service.AdminVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherService service;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@RequestBody VoucherRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(
            @PathVariable UUID id,
            @RequestBody VoucherRequest req) {

        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(service.getAll(page, size)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        service.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }
}