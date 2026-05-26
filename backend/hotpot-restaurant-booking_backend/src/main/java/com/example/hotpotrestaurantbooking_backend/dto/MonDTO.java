package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: MonDTO
// ==========================


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonDTO {

    private Integer idMon;

    private String tenMon;

    private BigDecimal donGiaHienTai;

    private Integer idDanhMuc;
}