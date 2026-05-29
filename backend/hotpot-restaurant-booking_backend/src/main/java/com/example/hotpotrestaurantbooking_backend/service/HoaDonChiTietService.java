package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;

import java.util.List;

public interface HoaDonChiTietService {
    List<DTOHoaDonChiTietResponse> getAll();
    DTOHoaDonChiTietResponse findById(Integer id);
    DTOHoaDonChiTietResponse add(DTOHoaDonChiTietRequest request);
    DTOHoaDonChiTietResponse update(Integer id, DTOHoaDonChiTietRequest request);
    void delete(Integer id);
    List<DTOHoaDonChiTietResponse> getChiTietByHoaDonId(Integer idHoaDon);
}
