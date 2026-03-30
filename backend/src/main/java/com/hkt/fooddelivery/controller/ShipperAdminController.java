package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.ShipperResponse;
import com.hkt.fooddelivery.service.ShipperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shippers")
@RequiredArgsConstructor
public class ShipperAdminController {

    private final ShipperAdminService service;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ShipperResponse>>> getPending() {
        List<ShipperResponse> data = service.getPending();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ShipperResponse>> approve(@PathVariable UUID id) {
        ShipperResponse response = service.approve(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Phê duyệt Shipper thành công."));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ShipperResponse>> reject(@PathVariable UUID id) {
        ShipperResponse response = service.reject(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã từ chối yêu cầu của Shipper."));
    }
}