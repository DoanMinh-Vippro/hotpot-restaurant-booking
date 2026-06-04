package com.example.hotpotrestaurantbooking_backend.service;


import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse;

import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;

public interface ChiTietGiamGiaMonService {
    List<ChiTietGiamGiaMonResponse> hienThiCTGGM();

    ChiTietGiamGiaMonResponse detailCTGGM(Integer idChiTietGiamGiaMon);

    Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(
            Integer pageNo,
            Integer pageSize
    );

    Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(
            String tenChuongTrinh,
            String tenMon,
            BigDecimal mucMin,
            BigDecimal mucMax,
            Integer pageNo,
            Integer pageSize
    );

    void addCTGGM(ChiTietGiamGiaMonRequest req);

    void updateCTGGM(
            Integer idChiTietGiamGiaMon,
            ChiTietGiamGiaMonRequest req
    );
}
