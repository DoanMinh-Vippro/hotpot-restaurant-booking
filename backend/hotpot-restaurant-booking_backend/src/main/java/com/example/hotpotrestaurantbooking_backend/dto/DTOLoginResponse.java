package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOLoginResponse {
    private String token;
    private Integer khachHangId;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private Boolean gioiTinh;
    private String maKhachHang;
    private String role;
}
