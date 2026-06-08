package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.CocResponse;
import com.example.hotpotrestaurantbooking_backend.service.CocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coc")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Khớp cổng với React/Vue của bạn
public class CocController {

    private final CocService cocService;

    @GetMapping
    public ResponseEntity<List<CocResponse>> findAll() {
        return ResponseEntity.ok(cocService.findAll());
    }

    @GetMapping("/trang-thai/{trangThaiCoc}")
    public ResponseEntity<List<CocResponse>> findByTrangThaiCoc(@PathVariable Integer trangThaiCoc) {
        return ResponseEntity.ok(cocService.findByTrangThaiCoc(trangThaiCoc));
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<CocResponse>> findByKhachHang(@PathVariable Integer khachHangId) {
        return ResponseEntity.ok(cocService.findByKhachHang(khachHangId));
    }

    @GetMapping("/tong-tien-da-thu")
    public ResponseEntity<BigDecimal> getTongTien() {
        return ResponseEntity.ok(cocService.tongTienCocDaThu());
    }
}