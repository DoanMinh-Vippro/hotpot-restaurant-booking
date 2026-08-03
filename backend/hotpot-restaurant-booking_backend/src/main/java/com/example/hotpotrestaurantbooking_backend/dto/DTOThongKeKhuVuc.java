package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOThongKeKhuVuc {
    private String khuVuc;
    private Long soHoaDon;
    private Double doanhThu;
    private Double trungBinhHoaDon;
}
