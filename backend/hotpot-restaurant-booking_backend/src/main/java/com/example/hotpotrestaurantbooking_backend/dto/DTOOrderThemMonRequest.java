package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrderThemMonRequest {
    private Integer idHoaDon;
    private Integer idMon;
    private Integer soLuong;
}
