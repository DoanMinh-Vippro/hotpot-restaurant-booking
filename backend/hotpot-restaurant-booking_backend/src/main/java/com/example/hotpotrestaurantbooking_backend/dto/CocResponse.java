package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CocResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private LocalDate ngayDat;
    private LocalTime gioDat;
    private Integer soNguoi;
    private BigDecimal soTienCoc;
    private Integer trangThaiCoc;
    private String trangThaiCocText;
    private Integer phuongThucThanhToan;
    private String phuongThucThanhToanText;
    private String ghiChu;
}