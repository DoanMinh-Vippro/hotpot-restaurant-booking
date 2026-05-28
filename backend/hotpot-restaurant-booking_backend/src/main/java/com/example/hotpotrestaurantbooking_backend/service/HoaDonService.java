// ===========================================
// HoaDonService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.HoaDonChiTietDTO;
import com.example.hotpotrestaurantbooking_backend.dto.HoaDonDTO;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;

import java.util.List;

public interface HoaDonService {

    List<HoaDonDTO> getAll();

    HoaDonDTO getById(Integer id);

    List<HoaDonChiTietDTO> getChiTietByHoaDonId(Integer idHoaDon);

    HoaDon add(HoaDon hoaDon);

    HoaDon update(Integer id, HoaDon hoaDon);

    void delete(Integer id);
}
