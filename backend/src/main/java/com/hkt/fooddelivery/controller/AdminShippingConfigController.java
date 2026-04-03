package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.ShippingConfigRequest;
import com.hkt.fooddelivery.dto.ShippingConfigResponse;
import com.hkt.fooddelivery.service.ShippingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shipping-configs")
@RequiredArgsConstructor
public class AdminShippingConfigController {

    private final ShippingConfigService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ShippingConfigResponse>> create(
            @RequestBody ShippingConfigRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingConfigResponse>> update(
            @PathVariable Integer id,
            @RequestBody ShippingConfigRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Integer id) {
        service.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Integer id) {
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShippingConfigResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(service.getAll(page, size)));
    }
}