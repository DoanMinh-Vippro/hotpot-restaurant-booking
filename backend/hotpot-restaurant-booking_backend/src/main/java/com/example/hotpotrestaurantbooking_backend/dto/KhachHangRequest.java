package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KhachHangRequest {
    private String maKhachHang;

    private String tenKhachHang;

    private Boolean gioiTinh;

    private String diaChi;

    private String soDienThoai;

    private String email;

    private Boolean trangThai;

    private TaiKhoan taiKhoan;
}
