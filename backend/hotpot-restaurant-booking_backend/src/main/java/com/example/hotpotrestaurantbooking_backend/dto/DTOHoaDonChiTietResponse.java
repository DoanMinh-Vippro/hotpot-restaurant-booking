package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOHoaDonChiTietResponse {
    private Integer idHoaDonChiTiet;
    private String maHoaDonChiTiet;
    private Integer idMon;
    private String tenMon;
    private Integer idCombo;
    private String tenCombo;
    private Integer idHoaDon;
    private Integer soLuong;
    private BigDecimal giaBanTaiThoiDiem;
    private BigDecimal tienGiamGiaMon;
    private BigDecimal thanhTien;
    private List<String> comboItems;
}
