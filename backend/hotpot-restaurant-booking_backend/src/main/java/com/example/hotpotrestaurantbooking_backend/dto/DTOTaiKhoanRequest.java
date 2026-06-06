package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DTOTaiKhoanRequest {
    @NotBlank(message = "tên đăng nhập không được bỏ trống")
    private String tenDangNhap;
    @NotBlank(message = "tên đng nập không được bỏ trống")
    @Size(min = 6, message = "mật khẩu tối thiểu 6 kí tự")
    private String matKhau;
}
