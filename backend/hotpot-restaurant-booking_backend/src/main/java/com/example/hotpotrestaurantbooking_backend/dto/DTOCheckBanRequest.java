package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DTOCheckBanRequest {

    private Integer soNguoi;

    private LocalDateTime thoiGianDenDuKien;
}