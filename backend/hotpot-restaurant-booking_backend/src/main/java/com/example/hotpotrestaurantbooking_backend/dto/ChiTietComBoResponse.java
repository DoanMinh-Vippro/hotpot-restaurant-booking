package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietComBoResponse {
    private Integer idChiTietCombo;
    private Integer soLuong;
    private Integer idMon;
    private String tenMon;
    private Integer idCombo;
    private String tenCombo;
    private BigDecimal giaCombo;
    private String hinhAnh;
    private Integer trangThai;
    private String moTa;
}
