package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;


import java.util.List;

public interface ThongKeService {

        List<DTOThongKeDoanhThu> theoNgay(String from, String to);

        List<DTOThongKeDoanhThu> theoThang(String from, String to);

        List<DTOThongKeDoanhThu> theoNam(String from, String to);

        List<DTOThongKeNhanVien> topNhanVien(String from, String to);

        List<DTOThongKeTheoMon> topMon(
                int page,
                int size,
                String from,
                String to
        );

        DTODashboard dashboard(String from, String to);

        List<DTOThongKeDoanhThu> tienCocTheoNgay(
                String from,
                String to
        );

        List<DTOTrangThaiCoc> trangThaiCoc(
                String from,
                String to
        );

        List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet(
                String from,
                String to
        );

        List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc(
                String from,
                String to
        );

        List<DTOThongKeHieuSuatBan> hieuSuatBan(
                String from,
                String to
        );

        List<DTOThongKeSanPhamBanChay> topSanPhamBanChay(
                String from,
                String to
        );

        List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai(
                String from,
                String to
        );

        List<DTOThongKeDoanhThu> doanhThuTheoGio(
                String from,
                String to
        );

        List<DTOThongKeHuyDatBan> tyLeHuyDatBan(
                String from,
                String to
        );

        List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc(
                String from,
                String to
        );
        byte[] exportExcel(String from, String to);
}
