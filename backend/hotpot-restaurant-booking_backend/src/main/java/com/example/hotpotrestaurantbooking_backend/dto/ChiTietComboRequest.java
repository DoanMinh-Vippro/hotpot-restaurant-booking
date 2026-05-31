package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ChiTietComboDTO
// ==========================

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietComboRequest {

    private Integer idChiTietCombo;
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Max(value = 30, message = "Số lượng không lớn hơn 30")
    private Integer soLuong;
    @NotNull(message = "Món không được để trống")
    private Integer idMon;
    @NotNull(message = "Combo không được để trống")
    private Integer idCombo;
    @NotBlank(message = "Mô tả không được để trống")
    private String moTa;
}