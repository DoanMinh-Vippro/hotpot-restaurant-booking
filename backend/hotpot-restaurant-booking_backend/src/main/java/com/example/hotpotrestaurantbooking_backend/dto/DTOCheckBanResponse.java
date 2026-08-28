package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class DTOCheckBanResponse {

    private String trangThai;

    private String message;

    private boolean canGhep;

    private Integer tongSucChua;

    // Bàn do hệ thống đề xuất
    private List<DTOBanResponse> dsBan;

    // Các bàn khách có thể tự lựa chọn
    private List<DTOBanResponse> dsBanTrong;
}