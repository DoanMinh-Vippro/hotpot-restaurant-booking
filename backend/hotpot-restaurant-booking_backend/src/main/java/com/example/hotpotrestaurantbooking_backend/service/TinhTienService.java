package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;

public interface TinhTienService {
    void ganThongTinGiamGia(MonResponse mon);

    void ganThongTinGiamGiaCombo(ComboResponse combo);
}
