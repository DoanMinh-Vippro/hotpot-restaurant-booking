package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: TaiKhoanDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanDTO {

    private Integer idTaiKhoan;

    private String maTaiKhoan;

    private String tenDangNhap;

    private String matKhau;

    private Boolean trangThai;
}