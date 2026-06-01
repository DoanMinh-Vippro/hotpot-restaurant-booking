package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: DotGiamGiaDTO
// ==========================


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotGiamGiaResponse {

    private Integer idDotGiamGia;

    private String tenChuongTrinh;

    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;
}