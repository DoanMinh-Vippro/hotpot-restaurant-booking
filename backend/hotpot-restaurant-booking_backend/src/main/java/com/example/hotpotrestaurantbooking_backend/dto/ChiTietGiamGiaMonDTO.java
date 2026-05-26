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
public class ChiTietGiamGiaMonDTO {

    private Integer idChiTietGiamGiaMon;

    private Integer idDotGiamGia;

    private Integer idMon;

    private BigDecimal mucGiam;
}