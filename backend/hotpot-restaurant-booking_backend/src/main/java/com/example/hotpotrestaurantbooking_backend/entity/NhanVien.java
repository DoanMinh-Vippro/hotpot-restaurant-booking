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
@Table(name = "NhanVien")
@Builder
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nhan_vien")
    private Integer id;
    @Column(name = "ma_nhan_vien")
    private String maNhanVien;
    @Column(name = "ten_nhan_vien")
    private String tenNhanVien;
    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    @Column(name = "email")
    private String email;
    @Column(name = "dia_chi")
    private String diaChi;
    @Column(name = "trang_thai")
    private Boolean trangThai;
    @ManyToOne
    @JoinColumn(name = "id_chuc_vu")
    private ChucVu chucVu;

    @ManyToOne @JoinColumn(name = "id_tai_khoan")
    private TaiKhoan taiKhoan;
}
