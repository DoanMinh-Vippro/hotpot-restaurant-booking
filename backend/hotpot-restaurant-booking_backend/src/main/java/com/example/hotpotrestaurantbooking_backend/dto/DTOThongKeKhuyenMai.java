package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeKhuyenMai {
    private String maGiamGia;
    private String loaiGiam;        // "PHANTRAM" hoặc "TIEN"
    private Double giaTriGiam;
    private Long soLanSuDung;
    private Double tongTienDaGiam;
    private Double tongDoanhThuSauGiam;
}
