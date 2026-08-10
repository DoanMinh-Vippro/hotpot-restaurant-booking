package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOCheckTinhTrangBanQuanLyResponse {

    private LocalDateTime thoiGianKiemTra;

    private int tongSoBan;
    private int soBanTrong;
    private int soBanDaDat;
    private int soBanDangSuDung;
    private int soBanBaoTri;

    private int tongSucChua;
    private int sucChuaConLai;

    private List<DTOBanResponse> dsBan;
}