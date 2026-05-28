package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: NhanVienDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienDTO {

    private Integer idNhanVien;

    private String maNhanVien;

    private String tenNhanVien;

    private Boolean gioiTinh;

    private String soDienThoai;

    private String email;

    private Integer idChucVu;

    private Integer idTaiKhoan;

    private String diaChi;

    private Boolean trangThai;
}