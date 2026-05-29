package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;

import java.util.List;

public interface BanService {
    List<DTOBanResponse> getAll();
    DTOBanResponse findById(Integer id);
    DTOBanResponse add(DTOBanRequest request);
    DTOBanResponse update(Integer id, DTOBanRequest request);
    void delete(Integer id);
    List<DTOBanResponse> search(String tenKhuVuc, String loaiBan);
    List<DTOBanResponse> sort();
}
