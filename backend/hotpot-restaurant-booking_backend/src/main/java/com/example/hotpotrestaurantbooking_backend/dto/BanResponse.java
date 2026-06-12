package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BanResponse {
    private Integer id;
    private String loaiBan;
    private Integer soLuongBan;
    private Integer trangThai;

    private Integer khuVucId;

}
