package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.RestaurantResponse;
import com.hkt.fooddelivery.service.RestaurantAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
public class RestaurantAdminController {

    private final RestaurantAdminService service;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success(service.getPendingRestaurants()));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RestaurantResponse>> approve(@PathVariable UUID id) {
        RestaurantResponse response = service.approve(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Nhà hàng đã được phê duyệt."));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RestaurantResponse>> reject(@PathVariable UUID id) {
        RestaurantResponse response = service.reject(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã từ chối yêu cầu của nhà hàng."));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<RestaurantResponse>> activate(@PathVariable UUID id) {
        RestaurantResponse response = service.activate(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Nhà hàng đã được kích hoạt hoạt động."));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<RestaurantResponse>> deactivate(@PathVariable UUID id) {
        RestaurantResponse response = service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã tạm dừng hoạt động nhà hàng."));
    }
}