package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.LoaiBan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private LoaiBan loaiBan;
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
    private List<DTOHoaDonChiTietResponse> chiTiet;// dun phục vụ cho thanh toán
}
