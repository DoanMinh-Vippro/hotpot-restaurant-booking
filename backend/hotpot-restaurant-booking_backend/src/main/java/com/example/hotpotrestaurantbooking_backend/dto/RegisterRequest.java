package org.example.datlich.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String tenDangNhap;
    private String matKhau;

    private String tenNhanVien;
    private Boolean gioiTinh;
    private String soDienThoai;
    private String email;
    private String diaChi;

    private Integer idChucVu;
}
