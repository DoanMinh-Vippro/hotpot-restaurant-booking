package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeKhachHangThanThiet {
    private String tenKhachHang;
    private String soDienThoai;
    private Long soLanDen;
    private Double tongChiTieu;
    private Double trungBinhHoaDon;
    private String lanCuoiDen;
}
