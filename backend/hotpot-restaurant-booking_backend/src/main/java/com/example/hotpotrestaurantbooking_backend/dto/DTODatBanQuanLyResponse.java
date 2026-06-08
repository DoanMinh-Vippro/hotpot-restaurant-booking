package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODatBanQuanLyResponse {
    private Integer idDatBan;
    private Integer idBan;
    private String loaiBan;
    private Integer idKhachHang;
    private String tenKhachHang;
    private LocalDate ngayDat;
    private LocalTime gioDat;
    private String sdtKhachHang;
    private int soNguoi;
    private TrangThaiDatBan trangThai;
    private String ghiChu;
    private LocalDateTime thoiGianDenDuKien;
    private BigDecimal soTienCoc;
    private TrangThaiDatBanCoc trangThaiCoc;
    private PhuongThucThanhToan phuongThucThanhToan;
}
