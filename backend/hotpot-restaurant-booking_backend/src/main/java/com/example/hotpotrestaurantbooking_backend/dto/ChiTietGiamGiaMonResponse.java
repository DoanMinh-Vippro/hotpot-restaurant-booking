package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ChiTietGiamGiaMonDTO
// ==========================


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGiamGiaMonResponse {

    private Integer idChiTietGiamGiaMon;
    private Integer idDotGiamGia;
    private String tenChuongTrinh;
    private Integer idMon;
    private String tenMon;

    private BigDecimal mucGiam;
}