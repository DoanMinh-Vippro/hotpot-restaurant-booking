package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.LoaiBan;
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
    private String tenBan;
    private LoaiBan loaiBan;
    private Integer idKhuVuc;
    private String tenKhuVuc;
    private TrangThaiBan trangThai;
}
