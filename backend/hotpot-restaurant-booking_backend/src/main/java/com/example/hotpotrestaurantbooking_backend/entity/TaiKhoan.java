    package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TaiKhoan")
@Builder
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tai_khoan")
    private Integer id;
    @Column(name = "ma_tai_khoan")
    private String maTaiKhoan;
    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;
    @Column(name = "mat_khau")
    private String matKhau;
    @Column(name = "trang_thai")
    private Boolean trangThai;
}
