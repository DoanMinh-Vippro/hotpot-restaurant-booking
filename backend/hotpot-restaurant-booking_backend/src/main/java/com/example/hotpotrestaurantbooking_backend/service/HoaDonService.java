package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOSplitHoaDonRequest;

import java.util.List;

public interface HoaDonService {
    List<DTOHoaDonResponse> getAll();
    DTOHoaDonResponse findById(Integer id);
    DTOHoaDonResponse add(DTOHoaDonRequest request);
    DTOHoaDonResponse update(Integer id, DTOHoaDonRequest request);
    void delete(Integer id);
    List<DTOHoaDonResponse> search(String keyword);
    List<DTOHoaDonResponse> findByKhachHangId(Integer khachHangId);
    List<DTOHoaDonResponse> getActiveTableInvoices();
    DTOHoaDonResponse findByBanAndStatus(Integer idBan, Integer trangThaiHoaDon);
    DTOHoaDonResponse split(Integer idHoaDon, DTOSplitHoaDonRequest request);
}
