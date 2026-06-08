package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTONhanVienResponse {
    private Integer id;
    private String maNhanVien;
    private String tenNhanVien;
    private Boolean gioiTinh;
    private String soDienThoai;
    private String email;
    private String tenChucVu;
    private String tenDangNhap;
    private Integer idChucVu;
    private Integer idTaiKhoan;
    private String diaChi;
    private Boolean trangThai;
}
