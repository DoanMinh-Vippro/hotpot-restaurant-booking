package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
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
    @Pattern(regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String soDienThoai;
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
            message = "Email phải đúng định dạng và có đuôi @gmail.com"
    )
    private String email;

    private Boolean trangThai;

    private TaiKhoan taiKhoan;
}
