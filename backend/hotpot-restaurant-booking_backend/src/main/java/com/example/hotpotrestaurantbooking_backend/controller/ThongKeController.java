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
public class ThongKeController {
    @Autowired
    private ThongKeService service;

    @GetMapping("/theo-ngay")
    public List<DTOThongKeDoanhThu> theoNgay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoNgay(from, to);
    }

    @GetMapping("/theo-thang")
    public List<DTOThongKeDoanhThu> theoThang() {
        return service.theoThang();
    }

    @GetMapping("/theo-nam")
    public List<DTOThongKeDoanhThu> theoNam() {
        return service.theoNam();
    }

    @GetMapping("/top-nhan-vien")
    public List<DTOThongKeNhanVien> topNhanVien() {
        return service.topNhanVien();
    }

    @GetMapping("/top-mon")
    public List<DTOThongKeTheoMon> topMon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return service.topMon(page, size);
    }

    @GetMapping("/dashboard")
    public DTODashboard dashboard() {
        return service.dashboard();
    }
}
