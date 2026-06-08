package com.example.hotpotrestaurantbooking_backend.service;

import org.example.datlich.dto.DTODashboard;
import org.example.datlich.dto.DTOThongKeDoanhThu;
import org.example.datlich.dto.DTOThongKeNhanVien;
import org.example.datlich.dto.DTOThongKeTheoMon;

import java.util.List;

public interface ThongKeService {
    List<DTOThongKeDoanhThu> theoNgay(String from, String to);

    List<DTOThongKeDoanhThu> theoThang();

    List<DTOThongKeDoanhThu> theoNam();

    List<DTOThongKeNhanVien> topNhanVien();

    List<DTOThongKeTheoMon> topMon(int page, int size);

    DTODashboard dashboard();
}
