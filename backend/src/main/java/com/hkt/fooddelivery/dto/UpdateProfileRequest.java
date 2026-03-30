package com.hkt.fooddelivery.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone,
        MultipartFile avatarFile
) {}