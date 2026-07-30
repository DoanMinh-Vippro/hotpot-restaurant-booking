package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrderItemResponse {
    private Integer idHoaDonChiTiet;
    private Integer idMon;
    private String tenMon;
    private Integer idCombo;
    private String tenCombo;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private boolean combo;
}
