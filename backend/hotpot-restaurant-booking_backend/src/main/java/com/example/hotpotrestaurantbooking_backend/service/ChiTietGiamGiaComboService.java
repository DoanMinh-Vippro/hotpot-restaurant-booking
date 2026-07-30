package com.example.hotpotrestaurantbooking_backend.service;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;

public interface ChiTietGiamGiaComboService {
    List<ChiTietGiamGiaComboResponse> hienThiCTGGCombo();

    ChiTietGiamGiaComboResponse detailCTGGCombo(Integer idChiTietGiamGiaCombo);

    Page<ChiTietGiamGiaComboResponse> phanTrangCTGGCombo(
            Integer pageNo,
            Integer pageSize
    );

    Page<ChiTietGiamGiaComboResponse> timKiemCTGGCombo(
            String tenChuongTrinh,
            String tenCombo,
            BigDecimal mucMin,
            BigDecimal mucMax,
            String loaiGiam,
            Integer pageNo,
            Integer pageSize
    );

    void addCTGGCombo(ChiTietGiamGiaComboRequest req);

    void updateCTGGCombo(
            Integer idChiTietGiamGiaCombo,
            ChiTietGiamGiaComboRequest req
    );
}
