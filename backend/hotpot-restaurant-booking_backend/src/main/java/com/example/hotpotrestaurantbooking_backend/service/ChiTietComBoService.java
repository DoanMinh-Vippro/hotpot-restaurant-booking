package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComboRequest;

import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;

public interface ChiTietComBoService {
    List<ChiTietComBoResponse> hienThi();

    ChiTietComBoResponse detailCTCB(Integer idChiTietCombo);

    Page<ChiTietComBoResponse> phanTrangCTCB(
            Integer pageNo,
            Integer pageSize
    );

    Page<ChiTietComBoResponse> timKiemCTCB(
            String tenCombo,
            String tenMon,
            BigDecimal giaMin,
            BigDecimal giaMax,
            Integer pageNo,
            Integer pageSize
    );

    void addCTCB(ChiTietComboRequest req);

    void updateCTCB(
            Integer idChiTietCombo,
            ChiTietComboRequest req
    );
}
