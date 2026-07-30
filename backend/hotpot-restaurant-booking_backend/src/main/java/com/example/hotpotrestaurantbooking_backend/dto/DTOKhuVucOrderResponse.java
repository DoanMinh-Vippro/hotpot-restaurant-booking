package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOKhuVucOrderResponse {

    private Integer idKhuVuc;

    private String tenKhuVuc;

    private List<DTOBanResponse> dsBan;
}