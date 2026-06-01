package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: DanhMucDTO
// ==========================


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucResquest {

    private Integer idDanhMuc;
    @NotBlank(message = "Loại danh mục không được để trống")
    @Size(min = 3, max = 50,
            message = "Loại danh mục phải từ 3 đến 50 ký tự")
    private String loaiDanhMuc;
    @NotBlank(message = "Mô tả không được để trống")
    private String moTa;
}