// ===========================================
// DatBanService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;

import java.util.List;

public interface DatBanService {
    List<DTODatBanResponse> getAll();
    DTODatBanResponse findById(Integer id);
    DTODatBanResponse add(DTODatBanRequest datBan);
    DTODatBanResponse update(Integer id,DTODatBanRequest datBan);
    void delete(Integer id);
    List<DTODatBanResponse> getDatBanByKhachHang(Integer id);
}