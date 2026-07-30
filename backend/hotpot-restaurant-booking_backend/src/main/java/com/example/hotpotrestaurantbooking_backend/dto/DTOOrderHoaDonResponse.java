package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrderHoaDonResponse {
    private Integer idHoaDon;
    private Integer idBan;
    private String tenBan;
    private Integer idDatBan;
    private List<DTOOrderItemResponse> items;
}
