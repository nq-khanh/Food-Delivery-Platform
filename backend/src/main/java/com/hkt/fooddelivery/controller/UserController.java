package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.dto.ApiResponse;
import com.hkt.fooddelivery.dto.UpdateProfileRequest;
import com.hkt.fooddelivery.dto.UserProfileResponse;
import com.hkt.fooddelivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(principal.getName())));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMe(Principal principal, @ModelAttribute UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(principal.getName(), req)));
    }
}
