package com.hkt.fooddelivery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StorageServiceIntegrationTest {

    @Autowired
    private StorageService storageService;

    @Test
    @DisplayName("Luồng tích hợp: Upload -> Kiểm tra URL -> Xóa file thật trên MinIO")
    void fullFlow_IntegrationTest() throws Exception {
        String content = "Dữ liệu test tích hợp cho Food Delivery Platform";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "integration-test-img.png",
                "image/png",
                content.getBytes()
        );

        String uploadedUrl = storageService.upload(file, "test-integration");
        System.out.println(">>> File đã được upload tại: " + uploadedUrl);

        assertNotNull(uploadedUrl);
        assertTrue(uploadedUrl.contains("test-integration"));

        URL url = new URL(uploadedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode, "URL phải có quyền truy cập công khai (Public Read)");

        String bucketName = "food-delivery-bucket";
        String objectKey = uploadedUrl.substring(uploadedUrl.indexOf(bucketName) + bucketName.length() + 1);

        assertTrue(storageService.exists(objectKey), "File phải tồn tại trên MinIO");

        storageService.deleteByUrl(uploadedUrl);
        System.out.println(">>> Đã ra lệnh xóa file qua URL thành công.");

        assertFalse(storageService.exists(objectKey), "File phải không còn tồn tại sau khi xóa");
    }
}
