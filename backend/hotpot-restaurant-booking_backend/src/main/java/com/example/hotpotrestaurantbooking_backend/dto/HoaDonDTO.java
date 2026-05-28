package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: HoaDonDTO
// ==========================

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonDTO {

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

    private Integer idKhachHang;

    private String tenKhachHang;

    private Integer idNhanVien;

    private String tenNhanVien;

    private Integer trangThaiThanhToan;

    private Integer phuongThucThanhToan;
}
