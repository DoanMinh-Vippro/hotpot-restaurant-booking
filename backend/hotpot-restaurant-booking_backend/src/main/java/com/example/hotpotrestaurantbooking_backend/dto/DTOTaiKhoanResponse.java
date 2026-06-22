package com.example.hotpotrestaurantbooking_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DTOTaiKhoanResponse {
    private Integer id;   // nên thêm luôn id
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private Boolean trangThai;
    private String tenChucVu;
    private Integer idChucVu;
}
