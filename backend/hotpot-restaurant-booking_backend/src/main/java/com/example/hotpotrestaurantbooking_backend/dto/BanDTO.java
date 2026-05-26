package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: BanDTO
// ==========================

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanDTO {

    private Integer idBan;

    private String loaiBan;

    private Integer soLuongBan;

    private Integer idKhuVuc;

    private Integer trangThai;
}