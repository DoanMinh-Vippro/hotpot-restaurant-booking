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
public class ChiTietComboDTO {

    private Integer idChiTietCombo;

    private Integer soLuong;

    private Integer idMon;

    private Integer idCombo;

    private String moTa;
}