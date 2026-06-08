package com.example.hotpotrestaurantbooking_backend.controller;


import com.example.hotpotrestaurantbooking_backend.dto.DTODashboard;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeDoanhThu;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeNhanVien;
import com.example.hotpotrestaurantbooking_backend.dto.DTOThongKeTheoMon;
import com.example.hotpotrestaurantbooking_backend.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/thong-ke")
@CrossOrigin(origins = "*")
public class ThongKeController {
    @Autowired
    private ThongKeService service;

    // ==============================
    // 1. Thống kê theo ngày (filter)
    // ==============================
    @GetMapping("/theo-ngay")
    public List<DTOThongKeDoanhThu> theoNgay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoNgay(from, to);
    }

    // ==============================
    // 2. Thống kê theo tháng
    // ==============================
    @GetMapping("/theo-thang")
    public List<DTOThongKeDoanhThu> theoThang() {
        return service.theoThang();
    }

    // ==============================
    // 3. Thống kê theo năm
    // ==============================
    @GetMapping("/theo-nam")
    public List<DTOThongKeDoanhThu> theoNam() {
        return service.theoNam();
    }

    // ==============================
    // 4. Top nhân viên
    // ==============================
    @GetMapping("/top-nhan-vien")
    public List<DTOThongKeNhanVien> topNhanVien() {
        return service.topNhanVien();
    }

    // ==============================
    // 5. Top món (có phân trang)
    // ==============================
    @GetMapping("/top-mon")
    public List<DTOThongKeTheoMon> topMon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return service.topMon(page, size);
    }

    // ==============================
    // 6. Dashboard tổng hợp
    // ==============================
    @GetMapping("/dashboard")
    public DTODashboard dashboard() {
        return service.dashboard();
    }
}
