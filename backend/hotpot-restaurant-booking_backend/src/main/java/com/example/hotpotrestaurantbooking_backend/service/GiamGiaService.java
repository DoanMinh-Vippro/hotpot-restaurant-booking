package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaDTO;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaRequest;

import java.util.List;

public interface GiamGiaService {
    List<GiamGiaDTO> getAll(String keyword);
    GiamGiaDTO getById(Integer idGiamGia);
    void createGiamGia(GiamGiaRequest request);
    void updateGiamGia(Integer idGiamGia, GiamGiaRequest request);
    void deleteGiamGia(Integer idGiamGia);
}
