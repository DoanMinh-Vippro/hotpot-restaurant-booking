package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class DTOCheckBanResponse {

    private String trangThai;

    private String message;

    private boolean canGhep;

    private Integer tongSucChua;

    private List<DTOBanResponse> dsBan;
}