package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "ko bỏ trống tên")
    private String tenKhachHang;

    private Boolean gioiTinh;

    private String diaChi;
    @NotBlank(message = "Số điện thoại không được để trống")
    // Regex kiểm tra số điện thoại VN: bắt đầu bằng 0 hoặc +84, theo sau là 9 chữ số
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String soDienThoai;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private Boolean trangThai;

    private TaiKhoan taiKhoan;
}
