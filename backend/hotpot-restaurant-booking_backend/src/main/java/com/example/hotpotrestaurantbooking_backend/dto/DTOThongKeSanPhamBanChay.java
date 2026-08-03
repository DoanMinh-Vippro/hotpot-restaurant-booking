package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeSanPhamBanChay {
    private String loai;        // "MÓN" hoặc "COMBO"
    private String tenSanPham;
    private Long soLuongBan;
    private Double tongThu;
}
