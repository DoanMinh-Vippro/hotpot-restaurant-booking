package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOCheckTinhTrangBanQuanLyRequest {

    @NotNull(message = "Thời gian kiểm tra không được để trống")
    private LocalDateTime thoiGianKiemTra;
}