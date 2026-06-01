package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"HoaDon\"")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hoa_don")
    private Integer idHoaDon;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "trang_thai_hoa_don")
    private Integer trangThaiHoaDon;

    @Column(name = "sdt_khach_hang")
    private String sdtKhachHang;

    @Column(name = "tien_truoc_giam")
    private BigDecimal tienTruocGiam;

    @Column(name = "tien_coc")
    private BigDecimal tienCoc;

    @Column(name = "tien_giam_gia")
    private BigDecimal tienGiamGia;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "thoi_gian_xuat")
    private LocalDateTime thoiGianXuat;

    @Column(name = "trang_thai_thanh_toan")
    private Integer trangThaiThanhToan;

    @Column(name = "phuong_thuc_thanh_toan")
    private Integer phuongThucThanhToan;

    @ManyToOne
    @JoinColumn(name = "id_ban")
    private Ban ban;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dat_ban")
    private DatBan datBan;

    @ManyToOne
    @JoinColumn(name = "id_giam_gia")
    private GiamGia giamGia;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
}
