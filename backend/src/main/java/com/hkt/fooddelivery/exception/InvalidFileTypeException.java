package com.hkt.fooddelivery.exception;

/**
 * Ném ra khi content type của file không nằm trong whitelist cấu hình.
 */
public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String contentType) {
        super("Content type không được phép: " + contentType);
    }
}
 