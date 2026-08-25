package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DTOSplitHoaDonRequest {
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        private Integer idHoaDonChiTiet;
        private Integer soLuong;
    }
}
