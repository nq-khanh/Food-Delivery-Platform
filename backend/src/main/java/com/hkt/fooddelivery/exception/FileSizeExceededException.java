package com.hkt.fooddelivery.exception;

/**
 * Ném ra khi kích thước file vượt quá giới hạn cho phép.
 */
public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(long actualSize, long maxSize) {
        super(String.format(
                "Kích thước file (%d bytes) vượt quá giới hạn cho phép (%d bytes).",
                actualSize, maxSize));
    }
}
 