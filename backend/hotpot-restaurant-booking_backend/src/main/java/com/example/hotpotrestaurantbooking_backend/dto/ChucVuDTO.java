package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ChucVuDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChucVuDTO {

    private Integer idChucVu;

    private String maChucVu;

    private String tenChucVu;
}