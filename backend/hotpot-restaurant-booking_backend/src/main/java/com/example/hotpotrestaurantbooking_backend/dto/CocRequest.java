package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CocRequest {
    private String maTienCoc;
    private BigDecimal soTien;
    private Integer phuongThucThanhToan;
    private String maGiaoDich;
    private String ghiChu;
    private Integer datBanId;
    private Integer khachHangId;
}