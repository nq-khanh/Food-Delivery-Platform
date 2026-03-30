package com.hkt.fooddelivery.dto;

public record UpdateVehicleRequest(
        String vehicleInfo,
        String licensePlate
) {}