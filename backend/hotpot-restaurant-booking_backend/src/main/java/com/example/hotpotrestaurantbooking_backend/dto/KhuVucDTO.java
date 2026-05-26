package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: KhuVucDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuVucDTO {

    private Integer idKhuVuc;

    private String moTa;

    private String tenKhuVuc;

    private Integer trangThai;
}