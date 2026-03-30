package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.LocationDTO;
import com.hkt.fooddelivery.dto.ShipperResponse;
import com.hkt.fooddelivery.dto.UpdateVehicleRequest;
import com.hkt.fooddelivery.service.ShipperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shippers")
@RequiredArgsConstructor
public class ShipperController {

    private final ShipperService service;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipperResponse>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getProfile(id)));
    }

    @PutMapping("/{id}/online")
    public ResponseEntity<ApiResponse<ShipperResponse>> goOnline(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.goOnline(id), "ONLINE_SUCCESS"));
    }

    @PutMapping("/{id}/offline")
    public ResponseEntity<ApiResponse<ShipperResponse>> goOffline(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.goOffline(id), "OFFLINE_SUCCESS"));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody LocationDTO location) {
        service.updateLocation(id, location);
        return ResponseEntity.ok(ApiResponse.success("LOCATION_UPDATED"));
    }

    @PatchMapping("/{id}/vehicle")
    public ResponseEntity<ApiResponse<ShipperResponse>> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateVehicle(id, req), "VEHICLE_UPDATED"));
    }

    @GetMapping("/nearest")
    public ResponseEntity<ApiResponse<List<ShipperResponse>>> getNearestShippers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") int limit) {

        LocationDTO location = new LocationDTO(lat, lng);
        List<ShipperResponse> data = service.findNearbyShippers(location, limit);

        return ResponseEntity.ok(ApiResponse.success(data, "Tìm thấy " + data.size() + " shipper gần nhất."));
    }
}