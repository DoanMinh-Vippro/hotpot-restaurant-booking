package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrderThemComboRequest {

    private Integer idHoaDon;

    private Integer idCombo;

    private Integer soLuong;

}
