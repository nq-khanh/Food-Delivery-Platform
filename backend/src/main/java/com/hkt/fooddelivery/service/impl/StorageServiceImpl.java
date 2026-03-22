package com.hkt.fooddelivery.service.impl;

import com.hkt.fooddelivery.config.MinioProperties;
import com.hkt.fooddelivery.exception.*;
import com.hkt.fooddelivery.service.StorageService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public StorageServiceImpl(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            String bucketName = properties.getBucketName();

            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!found) {
                log.info("Bucket '{}' không tồn tại. Đang tiến hành khởi tạo...", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );

                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "            \"Action\": [\"s3:GetBucketLocation\", \"s3:ListBucket\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "\"]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "            \"Action\": [\"s3:GetObject\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
                log.info("Khởi tạo Bucket và cấu hình Public Policy thành công.");
            }
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi khởi tạo MinIO Bucket: {}", e.getMessage());
            throw new RuntimeException("Could not initialize MinIO storage", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        validateFile(file);

        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "file";
        String objectKey = buildObjectKey(folder, originalFilename);

        log.info("Bắt đầu upload lên MinIO: key={}, size={}", objectKey, file.getSize());

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectKey)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return buildUrl(objectKey);

        } catch (Exception e) {
            log.error("Lỗi upload file: {}", e.getMessage());
            throw new StorageUploadException("Không thể upload file lên hệ thống storage", e);
        }
    }

    @Override
    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) return;

        try {
            String objectKey = parseObjectKeyFromUrl(url);

            log.info("Xóa file trên MinIO: key={}", objectKey);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi khi xóa file qua URL {}: {}", url, e.getMessage());
            throw new StorageDeleteException("Xóa file thất bại");
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) return false;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Private Helpers ---

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new StorageUploadException("File không được để trống");

        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new FileSizeExceededException(file.getSize(), properties.getMaxFileSizeBytes());
        }

        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedContentTypes().contains(contentType)) {
            throw new InvalidFileTypeException(contentType);
        }
    }

    private String buildObjectKey(String folder, String filename) {
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String sanitizedName = filename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        return String.format("%s/%s/%s_%s", folder, datePath, uuid, sanitizedName);
    }

    private String buildUrl(String objectKey) {
        String endpoint = properties.getEndpoint().replaceAll("/$", "");
        return String.format("%s/%s/%s", endpoint, properties.getBucketName(), objectKey);
    }

    private String parseObjectKeyFromUrl(String url) {
        String bucketName = properties.getBucketName();
        String marker = bucketName + "/";
        int index = url.indexOf(marker);

        if (index == -1) {
            throw new StorageDeleteException("URL không hợp lệ hoặc không thuộc bucket: " + bucketName);
        }

        return url.substring(index + marker.length());
    }
}