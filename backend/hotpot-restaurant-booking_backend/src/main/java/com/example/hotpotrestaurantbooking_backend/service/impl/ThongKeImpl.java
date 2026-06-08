package com.example.hotpotrestaurantbooking_backend.service;



import com.example.hotpotrestaurantbooking_backend.dto.DTODashboard;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeDoanhThu;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeNhanVien;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeTheoMon;
import com.example.hotpotrestaurantbooking_backend.repository.ThongKeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongKeImpl implements ThongKeService {
    @Autowired
    private ThongKeRepository repo;

    // ===== 1. Doanh thu theo ngày =====
    @Override
    public List<DTOThongKeDoanhThu> theoNgay(String from, String to) {
        return repo.thongKeTheoNgay(from, to).stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        Double.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 2. Doanh thu theo tháng =====
    @Override
    public List<DTOThongKeDoanhThu> theoThang() {
        return repo.thongKeTheoThang().stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        Double.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 3. Doanh thu theo năm =====
    @Override
    public List<DTOThongKeDoanhThu> theoNam() {
        return repo.thongKeTheoNam().stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        Double.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 4. Top nhân viên =====
    @Override
    public List<DTOThongKeNhanVien> topNhanVien() {
        return repo.topNhanVien().stream()
                .map(o -> new DTOThongKeNhanVien(
                        o[0].toString(),
                        Double.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 5. Top món (có pagination) =====
    @Override
    public List<DTOThongKeTheoMon> topMon(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return repo.topMon(pageable).stream()
                .map(o -> new DTOThongKeTheoMon(
                        o[0].toString(),
                        Integer.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 6. Dashboard tổng =====
    @Override
    public DTODashboard dashboard() {
        Object[] o = (Object[]) repo.dashboard();

        return new DTODashboard(
                o[0] != null ? Double.valueOf(o[0].toString()) : 0,
                o[1] != null ? Long.valueOf(o[1].toString()) : 0,
                o[2] != null ? Long.valueOf(o[2].toString()) : 0
        );
    }
}
