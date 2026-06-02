package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.MonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;

import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;


public interface MonService {
    List<MonResponse> hienThiMon();

    MonResponse detailMon(String tenMon);

    Page<MonResponse> phanTrangMon(
            Integer pageNo,
            Integer pageSize
    );

    Page<MonResponse> timKiemMon(
            String tenMon,
            BigDecimal giaMin,
            BigDecimal giaMax,
            String loaiDanhMuc,
            Integer pageNo,
            Integer pageSize
    );

    void addMon(MonRequest req);

    void updateMon(Integer idMon,
                   MonRequest req);

    void deleteMon(Integer idMon);
}
