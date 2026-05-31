package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ComboDTO
// ==========================

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboResponse {

    private Integer idCombo;

    private String tenCombo;

    private BigDecimal giaCombo;

    private String hinhAnh;

    private Integer trangThai;
}