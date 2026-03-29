package com.hkt.fooddelivery.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;


/**
 * Khởi tạo bean {@link MinioClient} từ cấu hình {@link MinioProperties}.
 * Bean này được inject vào {@code StorageServiceImpl} và {@code MinioHealthIndicator}.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {
 
    private final MinioProperties properties;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }
 
    @Bean
    public MinioClient minioClient() {
        log.info("Khởi tạo MinioClient: endpoint={}, bucket={}", properties.getEndpoint(), properties.getBucketName());
 
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}