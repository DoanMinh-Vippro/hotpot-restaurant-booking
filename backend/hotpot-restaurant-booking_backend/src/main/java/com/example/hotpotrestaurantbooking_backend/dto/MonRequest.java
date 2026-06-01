package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonRequest {
    private Integer idMon;
    @NotBlank(message = "Tên món không được để trống")
    @Size(min = 3, max = 40,
            message = "Tên món phải từ 3 đến 40 ký tự")
    private String tenMon;
    @NotNull(message = "Đơn giá không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal donGiaHienTai;
    @NotNull(message = "Danh mục không được để trống")
    private Integer idDanhMuc;
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
