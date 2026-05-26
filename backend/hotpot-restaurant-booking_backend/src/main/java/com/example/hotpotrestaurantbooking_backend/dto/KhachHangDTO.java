package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: KhachHangDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangDTO {

    private Integer idKhachHang;

    private String maKhachHang;

    private String tenKhachHang;

    private Boolean gioiTinh;

    private String diaChi;

    private String soDienThoai;

    private Integer idTaiKhoan;

    private String email;

    private Boolean trangThai;
}