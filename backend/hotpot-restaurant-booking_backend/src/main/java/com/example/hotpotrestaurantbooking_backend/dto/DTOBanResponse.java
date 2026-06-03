package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOBanResponse {
    private Integer idBan;
    private String loaiBan;
    private int soLuongBan;
    private Integer idKhuVuc;
    private String tenKhuVuc;
    private TrangThaiBan trangThai;
}
