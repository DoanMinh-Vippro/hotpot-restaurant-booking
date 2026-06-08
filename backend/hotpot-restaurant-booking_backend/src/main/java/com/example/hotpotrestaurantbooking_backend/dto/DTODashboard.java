package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTODashboard {
    private Double tongDoanhThu;
    private Long tongHoaDon;
    private Long tongKhachHang;
}
