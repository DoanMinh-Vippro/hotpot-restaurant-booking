package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"NhanVien\"")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNhanVien;
    private String maNhanVien;
    private String tenNhanVien;
    private Boolean gioiTinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private Boolean trangThai;

    @ManyToOne @JoinColumn(name = "id_chuc_vu")
    private ChucVu chucVu;

    @ManyToOne @JoinColumn(name = "id_tai_khoan")
    private TaiKhoan taiKhoan;
}
