package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;

import java.time.LocalDateTime;
import java.util.List;

public interface DatBanService {
    List<DTODatBanResponse> getAll();
    DTODatBanResponse findById(Integer id);
    DTODatBanResponse add(DTODatBanRequest datBan);
    DTODatBanResponse update(Integer id,DTODatBanRequest datBan);
    void delete(Integer id);
    List<DTODatBanResponse> getDatBanByKhachHang(Integer id);
    DatBan createBookingAfterPayment(Integer idKhachHang, DTODatBanRequest datBan);
    DTOCheckBanResponse checkBan(DTOCheckBanRequest request);
    DTOTinhTrangBanResponse tinhTrangBan(LocalDateTime thoiGianDenDuKien);
}