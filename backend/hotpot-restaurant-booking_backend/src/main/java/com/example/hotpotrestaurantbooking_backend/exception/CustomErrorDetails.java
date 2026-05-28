package com.example.hotpotrestaurantbooking_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomErrorDetails {
    private LocalDateTime time;
    private String message;
    private String details;
}
