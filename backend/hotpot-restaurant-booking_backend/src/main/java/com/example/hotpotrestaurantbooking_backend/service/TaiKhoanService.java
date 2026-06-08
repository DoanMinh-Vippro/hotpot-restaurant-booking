
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;


import java.util.List;

public interface TaiKhoanService {
    List<DTOTaiKhoanResponse> getAll();
    DTOTaiKhoanResponse findById(Integer id);
    DTOTaiKhoanResponse add(DTOTaiKhoanRequest tk);
    DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest tk);




    void delete(Integer id);
}