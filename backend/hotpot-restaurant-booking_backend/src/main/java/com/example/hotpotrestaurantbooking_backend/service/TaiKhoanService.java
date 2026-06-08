package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;


import java.util.List;

public interface TaiKhoanService {
    List<DTOTaiKhoanResponse> getAll();

    DTOTaiKhoanResponse findById(Integer id);

    DTOTaiKhoanResponse add(DTOTaiKhoanRequest request);

    DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest
            request);

    void delete(Integer id);
}
