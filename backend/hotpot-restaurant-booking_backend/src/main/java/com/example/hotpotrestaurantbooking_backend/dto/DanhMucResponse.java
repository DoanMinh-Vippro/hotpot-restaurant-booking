package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: DanhMucDTO
// ==========================


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucResponse {

    private Integer idDanhMuc;

    private String loaiDanhMuc;

    private String moTa;
}