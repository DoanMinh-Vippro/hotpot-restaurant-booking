package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTODashboard;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeDoanhThu;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeNhanVien;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeTheoMon;


import java.util.List;

public interface ThongKeService {
    List<DTOThongKeDoanhThu> theoNgay(String from, String to);

    List<DTOThongKeDoanhThu> theoThang();

    List<DTOThongKeDoanhThu> theoNam();

    List<DTOThongKeNhanVien> topNhanVien();

    List<DTOThongKeTheoMon> topMon(int page, int size);

    DTODashboard dashboard();
}
