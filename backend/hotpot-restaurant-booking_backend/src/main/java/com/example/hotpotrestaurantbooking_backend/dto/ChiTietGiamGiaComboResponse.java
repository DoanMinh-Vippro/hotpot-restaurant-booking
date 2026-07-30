package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGiamGiaComboResponse {
    private Integer idChiTietGiamGiaCombo;
    private Integer idDotGiamGia;
    private String tenChuongTrinh;
    private Integer idCombo;
    private String tenCombo;
    private BigDecimal mucGiam;
    private String loaiGiam;
    private Integer trangThai;
}
