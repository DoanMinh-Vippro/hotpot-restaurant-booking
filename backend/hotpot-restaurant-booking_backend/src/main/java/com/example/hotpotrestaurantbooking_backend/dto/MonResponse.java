package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonResponse {
    private Integer idMon;
    private String tenMon;
    private BigDecimal donGiaHienTai;
    private Integer idDanhMuc;
    private String loaiDanhMuc;
    private Integer trangThai;
}
