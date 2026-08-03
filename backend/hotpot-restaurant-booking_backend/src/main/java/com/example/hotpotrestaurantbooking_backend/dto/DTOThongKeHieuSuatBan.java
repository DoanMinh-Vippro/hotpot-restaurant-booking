package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeHieuSuatBan {
    private String tenBan;
    private String khuVuc;
    private Long soLanPhucVu;
    private Double tongDoanhThu;
    private Double doanhThuTrungBinh;
}
