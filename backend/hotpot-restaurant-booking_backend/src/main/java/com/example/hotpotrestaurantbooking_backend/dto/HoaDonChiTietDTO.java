package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: HoaDonChiTietDTO
// ==========================


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonChiTietDTO {

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
}
