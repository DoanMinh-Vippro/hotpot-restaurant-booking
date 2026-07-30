package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOOrderHoaDonChiTietResponse {
    private Integer idHoaDon;
    private BigDecimal tongTien;
    private List<DTOOrderItemResponse> dsMon = new ArrayList<>();
    private List<DTOOrderItemResponse> dsCombo = new ArrayList<>();
}
