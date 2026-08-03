package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MayInRequest {
    private String tenQuay;
    private String tenBan;
    private String maHoaDon;
    private String tenNhanVien;
    private String thoiGian;
    private List<MonIn> danhSachMon;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonIn {
        private String tenMon;
        private int soLuong;
    }
}
