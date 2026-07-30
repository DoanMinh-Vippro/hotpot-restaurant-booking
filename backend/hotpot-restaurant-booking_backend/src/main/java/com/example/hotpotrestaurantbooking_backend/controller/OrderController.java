package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.service.OrderSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderSevice orderSevice;

    @GetMapping("/ban")
    public ResponseEntity<List<DTOKhuVucOrderResponse>> getDanhSachBanOrder() {
        return ResponseEntity.ok(orderSevice.getDanhSachBanOrder());
    }

    @GetMapping("/hoa-don")
    public ResponseEntity<DTOOrderHoaDonResponse> chonBan(@RequestParam Integer idBan) {
        return ResponseEntity.ok(orderSevice.chonBan(idBan));
    }

    @GetMapping("/menu")
    public ResponseEntity<DTOOrderMenuResponse> getMenu() {
        return ResponseEntity.ok(orderSevice.getMenu());
    }

    @PostMapping("/them-mon")
    public ResponseEntity<Void> themMon(@RequestBody DTOOrderThemMonRequest request) {
        orderSevice.themMon(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/them-combo")
    public ResponseEntity<Void> themCombo(@RequestBody DTOOrderThemComboRequest request) {
        orderSevice.themCombo(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hoa-don/{idHoaDon}/chi-tiet")
    public DTOOrderHoaDonChiTietResponse getChiTietHoaDon(@PathVariable Integer idHoaDon) {
        return orderSevice.getChiTietHoaDon(idHoaDon);
    }
}