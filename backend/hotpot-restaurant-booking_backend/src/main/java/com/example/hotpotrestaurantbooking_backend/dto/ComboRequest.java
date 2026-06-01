package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ComboDTO
// ==========================

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboRequest {

    private Integer idCombo;
    @NotBlank(message = "Tên combo không được để trống")
    @Size(min = 3, max = 50,
            message = "Tên combo phải từ 3 đến 50 ký tự")
    private String tenCombo;
    @NotNull(message = "Giá combo không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá combo phải lớn hơn 0")
    private BigDecimal giaCombo;
    @NotBlank(message = "Hình ảnh không được để trống")
    private String hinhAnh;
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}