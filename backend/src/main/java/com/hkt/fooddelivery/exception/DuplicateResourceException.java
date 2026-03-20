package com.hkt.fooddelivery.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, Object identifier) {
        super(resource + " đã tồn tại với: " + identifier);
    }

}
