package com.example.hotpotrestaurantbooking_backend.dto;


import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;

    private String tenKhachHang;

    private Boolean gioiTinh;

    private String diaChi;

    private String soDienThoai;

    private String email;

    private Boolean trangThai;

    private TaiKhoan taiKhoan;

    private List<DatBanResponse> datBanList;
}
