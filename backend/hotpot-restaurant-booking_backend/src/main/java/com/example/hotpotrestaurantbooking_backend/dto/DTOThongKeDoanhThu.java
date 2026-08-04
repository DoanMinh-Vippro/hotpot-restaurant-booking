package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeDoanhThu {
    private String thoiGian;
    private Long soHoaDon;
    private Double tongTien;
    private Double giamGia;
    private Double doanhThu;

        public DTOThongKeDoanhThu(String thoiGian, Double doanhThu) {
            this.thoiGian = thoiGian;
            this.doanhThu = doanhThu;
        }
}
