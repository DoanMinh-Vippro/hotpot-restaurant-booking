package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOHoaDonResponse {
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
    private Integer idBan;
    private String loaiBan;
    private Integer idDatBan;
    private Integer idGiamGia;
    private String maGiamGia;
    private String loaiGiam;
    private Integer idKhachHang;
    private String tenKhachHang;
    private Integer idNhanVien;
    private String tenNhanVien;
    private Integer trangThaiThanhToan;
    private Integer phuongThucThanhToan;
}
