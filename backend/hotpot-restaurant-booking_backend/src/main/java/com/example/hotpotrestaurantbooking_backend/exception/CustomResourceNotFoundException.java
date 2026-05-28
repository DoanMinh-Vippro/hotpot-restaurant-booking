package com.example.hotpotrestaurantbooking_backend.exception;

public class CustomResourceNotFoundException extends RuntimeException {
    public CustomResourceNotFoundException(String message) {
        super(message);
    }
}
