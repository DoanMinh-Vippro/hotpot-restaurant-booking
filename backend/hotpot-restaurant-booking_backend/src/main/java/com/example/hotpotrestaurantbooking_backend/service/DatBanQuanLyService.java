package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;

import java.time.LocalDateTime;
import java.util.List;

public interface DatBanQuanLyService {

    List<DTODatBanQuanLyResponse> getAll();

    DTODatBanQuanLyResponse findById(Integer id);

    DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d);

    DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d, TaiKhoan taiKhoan);

    DTODatBanQuanLyResponse update(Integer id, DTODatBanQuanLyRequest d);

    void delete(Integer id);

    List<DTOBanResponse> getDanhSachBanTrong(
            LocalDateTime thoiGianDenDuKien,
            Integer soNguoi,
            Integer idDatBan
    );

    DTODatBanQuanLyResponse xacNhan(Integer id);

    DTODatBanQuanLyResponse checkIn(Integer id);

    DTODatBanQuanLyResponse doiBan(Integer id, DTODoiBanRequest request);

    DTODatBanQuanLyResponse doiGio(Integer id, DTODoiGioRequest request);

    List<DTODatBanQuanLyResponse> search(
            String keyword,
            TrangThaiDatBan trangThai,
            LocalDateTime tuNgay,
            LocalDateTime denNgay
    );
}
