package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Chỉ dùng khi bạn muốn tạo full 5 tham số
@NoArgsConstructor
public class DTOThongKeDoanhThu {
    private String thoiGian;
    private Long soHoaDon;
    private Double doanhThu;
    private Double giaTri1;
    private Double giaTri2;

    // TỰ THÊM CONSTRUCTOR 2 THAM SỐ VÀO ĐÂY
    public DTOThongKeDoanhThu(String thoiGian, Double doanhThu) {
        this.thoiGian = thoiGian;
        this.doanhThu = doanhThu;
    }
}