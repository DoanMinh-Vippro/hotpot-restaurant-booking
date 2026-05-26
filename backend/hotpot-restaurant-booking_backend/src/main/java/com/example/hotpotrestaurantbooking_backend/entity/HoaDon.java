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
    private Integer idHoaDon;
    private String maHoaDon;
    private String maGiaoDich;
    private Integer trangThaiHoaDon;
    private String sdtKhachHang;
    private BigDecimal tienTruocGiam;
    private BigDecimal tienCoc;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTien;
    private LocalDateTime thoiGianXuat;
    private Integer trangThaiThanhToan;
    private Integer phuongThucThanhToan;

    @ManyToOne @JoinColumn(name = "id_ban") private Ban ban;
    @ManyToOne
    @JoinColumn(name = "id_dat_ban") private DatBan datBan;
    @ManyToOne @JoinColumn(name = "id_giam_gia") private GiamGia giamGia;
    @ManyToOne @JoinColumn(name = "id_khach_hang") private KhachHang khachHang;
    @ManyToOne @JoinColumn(name = "id_nhan_vien") private NhanVien nhanVien;
}
