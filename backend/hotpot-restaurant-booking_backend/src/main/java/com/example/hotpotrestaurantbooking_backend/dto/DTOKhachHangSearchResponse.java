package com.example.hotpotrestaurantbooking_backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOKhachHangSearchResponse {
    private Integer idKhachHang;

    private String tenKhachHang;

    private String soDienThoai;
}
