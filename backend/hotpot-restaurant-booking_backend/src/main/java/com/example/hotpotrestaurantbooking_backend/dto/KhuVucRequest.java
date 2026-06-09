package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KhuVucRequest {
    @NotBlank(message = "Tên khu vực không được để trống")
    @Size(max = 30, message = "Tên khu vực không quá 30 ký tự")
    private String tenKhuVuc;

    @Size(max = 100, message = "Mô tả không quá 100 ký tự")
    private String moTa;

    private Integer trangThai;  // 0 hoặc 1
}
