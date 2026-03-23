package com.hkt.fooddelivery.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile file, String folder);
    void deleteByUrl(String url);
    boolean exists(String objectKey);
}