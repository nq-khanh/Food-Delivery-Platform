package com.hkt.fooddelivery.exception;

/**
 * Ném ra khi xoá object khỏi MinIO thất bại.
 */
public class StorageDeleteException extends RuntimeException {
    public StorageDeleteException(String message) {
        super(message);
    }
    public StorageDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
 