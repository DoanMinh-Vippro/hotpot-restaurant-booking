package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.Email;
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
public class DTORegisterRequest {
    // Thông tin tài khoản
    @NotBlank(message = "Tên đăng nhập không được bỏ trống")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được bỏ trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String matKhau;

    // Thông tin khách hàng
    @NotBlank(message = "Tên khách hàng không được bỏ trống")
    private String tenKhachHang;

    private Boolean gioiTinh; // true: nam, false: nữ

    @NotBlank(message = "Số điện thoại không được bỏ trống")
    private String soDienThoai;

    @NotBlank(message = "Email không được bỏ trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Địa chỉ không được bỏ trống")
    private String diaChi;
}
