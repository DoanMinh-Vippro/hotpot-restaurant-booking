package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ChiTietGiamGiaMonDTO
// ==========================


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGiamGiaMonRequest {

    private Integer idChiTietGiamGiaMon;
    @NotNull(message = "Đợt giảm giá không đươc để trống")
    private Integer idDotGiamGia;
    @NotNull(message = "Món không đươc để trống")
    private Integer idMon;
    @NotNull(message = "Mức giảm không được để trống")
    @Positive(message = "Mức giảm phải lớn hơn 0")
    private BigDecimal mucGiam;
    @NotBlank(message = "Loại giảm không được để trống")
    private String loaiGiam; // "TIEN" hoặc "PHANTRAM"
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai; // 1: Kích hoạt, 0: Hủy áp dụng
}