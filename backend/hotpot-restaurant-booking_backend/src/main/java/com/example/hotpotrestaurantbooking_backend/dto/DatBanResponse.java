package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DatBanResponse {
    private Integer id;
    private LocalDate ngayDat;
    private LocalTime gioDat;
    private String sdtKhachHang;
    private Integer soNguoi;
    private Integer trangThai;
    private String trangThaiText;
    private String ghiChu;
    private LocalDateTime thoiGianDenDuKien;
    private String tenKhachHang;
    private String tenBan;

    // Tiền cọc
    private BigDecimal soTienCoc;
    private Integer trangThaiCoc;
    private String trangThaiCocText;
    private String phuongThucThanhToan;
    private String maGiaoDich;
    private LocalDateTime ngayCoc;
    private LocalDateTime ngayHoanCoc;
    private String lyDoHoanCoc;
}