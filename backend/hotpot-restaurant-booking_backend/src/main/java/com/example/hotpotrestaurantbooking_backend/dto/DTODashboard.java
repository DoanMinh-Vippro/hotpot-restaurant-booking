package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTODashboard {
    private Double tongDoanhThu;
    private Double doanhThuTienMat;
    private Double doanhThuChuyenKhoan;
    private Long tongHoaDon;
    private Long tongKhachHang;
    private Double tongTienCoc;
    private Long soDonDaCoc;
    private Long soDonChuaCoc;
    private Long soDonDaThanhToan;
}
