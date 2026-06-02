package com.example.hotpotrestaurantbooking_backend.service;


import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResquest;

import org.springframework.data.domain.Page;


import java.util.List;

public interface DanhMucService {
    List<DanhMucResponse> hienThiDM();

    DanhMucResponse detailDM(String loaiDanhMuc);

    Page<DanhMucResponse> phanTrangDM(Integer pageNo, Integer pageSize);

    Page<DanhMucResponse> timKiemDM(
            String loaiDanhMuc,
            Integer pageNo,
            Integer pageSize
    );

    void addDM(DanhMucResquest req);

    void updateDM(Integer idDanhMuc, DanhMucResquest req);
}
