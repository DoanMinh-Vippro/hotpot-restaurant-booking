package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ChiTietComboDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietComboRequest {

    private Integer idChiTietCombo;

    private Integer soLuong;

    private String idMon;

    private String idCombo;

    private String moTa;
}