package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrderMenuResponse {
    private List<MonResponse> dsMon;
    private List<ComboResponse> dsCombo;

}
