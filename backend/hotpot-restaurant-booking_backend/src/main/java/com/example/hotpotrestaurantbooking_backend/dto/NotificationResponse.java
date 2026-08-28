package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;
    private Integer targetKhachHangId;
    private boolean targetStaff;
}