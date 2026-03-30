package com.hkt.fooddelivery.dto;

public record ResetPasswordRequest(String token, String newPassword) {}