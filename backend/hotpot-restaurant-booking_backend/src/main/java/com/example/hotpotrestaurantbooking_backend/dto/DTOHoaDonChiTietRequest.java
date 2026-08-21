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
public class DTOHoaDonChiTietRequest {
    private String maHoaDonChiTiet;
    private Integer idMon;
    private Integer idCombo;
    private Integer idHoaDon;
    private Integer soLuong;
    private BigDecimal giaBanTaiThoiDiem;
    private BigDecimal tienGiamGiaMon;
    private BigDecimal thanhTien;
    private LocalDateTime orderedAt;
    private String orderedBy;
}
