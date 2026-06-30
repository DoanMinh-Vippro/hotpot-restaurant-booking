package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;

import java.util.List;

public interface DatBanQuanLyService {
    List<DTODatBanQuanLyResponse> getAll();
    DTODatBanQuanLyResponse findById(Integer id);
    DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d);
    DTODatBanQuanLyResponse update(Integer id,DTODatBanQuanLyRequest d);
    void delete(Integer id);
}
