package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.*;
import com.hkt.fooddelivery.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/send-link")
    public ResponseEntity<ApiResponse<Void>> sendRegistrationLink(
            @RequestParam String email,
            @RequestParam(defaultValue = "user") String origin) {

        authService.sendRegistrationLink(email, origin);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Link xác nhận đăng ký đã được gửi vào email của bạn."));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest req) {
        AuthResponse response = authService.completeRegistration(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email,
            @RequestParam(defaultValue = "user") String origin) {

        authService.sendResetPasswordLink(email, origin);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Link khôi phục mật khẩu đã được gửi qua email."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được cập nhật thành công."));
    }
}