package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KhuVucResponse {
    private Integer id;
    private String tenKhuVuc;
    private String moTa;
    private Integer trangThai;
 // Tổng số bàn trong khu vực
    private List<BanResponse> banList;
}
