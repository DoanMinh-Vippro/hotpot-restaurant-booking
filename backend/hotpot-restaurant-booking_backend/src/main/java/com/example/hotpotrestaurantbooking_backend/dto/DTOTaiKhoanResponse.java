package com.example.hotpotrestaurantbooking_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOTaiKhoanResponse {
    private Integer id;
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private Boolean trangThai;
}
