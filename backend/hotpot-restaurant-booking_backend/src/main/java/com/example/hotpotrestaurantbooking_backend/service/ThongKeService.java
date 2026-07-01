package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;


import java.util.List;

public interface ThongKeService {
    List<DTOThongKeDoanhThu> theoNgay(String from, String to);

    List<DTOThongKeDoanhThu> theoThang();

    List<DTOThongKeDoanhThu> theoNam();

    List<DTOThongKeNhanVien> topNhanVien();

    List<DTOThongKeTheoMon> topMon(int page, int size);

    DTODashboard dashboard();
    List<DTOThongKeDoanhThu> tienCocTheoNgay();

    List<DTOTrangThaiCoc> trangThaiCoc();
}
