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
public class DTOBanRequest {
    private LoaiBan loaiBan;
    private String tenBan;
    private Integer idKhuVuc;
    private TrangThaiBan trangThai;
}
