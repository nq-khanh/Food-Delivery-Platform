package com.hkt.fooddelivery.exception;

/**
 * Ném ra khi upload object lên MinIO thất bại.
 */
public class StorageUploadException extends RuntimeException {
    public StorageUploadException(String message) {
        super(message);
    }
    public StorageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
