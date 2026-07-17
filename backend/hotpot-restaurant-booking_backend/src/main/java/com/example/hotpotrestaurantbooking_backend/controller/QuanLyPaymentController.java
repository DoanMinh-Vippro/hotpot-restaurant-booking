package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOPaymentResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOVNPayResponse;
import com.example.hotpotrestaurantbooking_backend.service.QuanLyPaymentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/quan-ly-payment")
@RequiredArgsConstructor
public class QuanLyPaymentController {

    private final QuanLyPaymentService quanLyPaymentService;

    @PostMapping("/create")
    public DTOPaymentResponse createPayment(
            @RequestBody DTODatBanQuanLyRequest dto) {

        return quanLyPaymentService.createPayment(dto);
    }

    @GetMapping("/status")
    public boolean checkPaymentStatus(@RequestParam String content) {
        return quanLyPaymentService.checkPaymentStatus(content);
    }

    @PostMapping("/vnpay/create")
    public ResponseEntity<DTOVNPayResponse> createVNPay(
            @RequestBody @Valid DTODatBanQuanLyRequest dto) {

        return ResponseEntity.ok(
                quanLyPaymentService.createVNPayPayment(dto)
        );
    }

    @GetMapping("/vnpay-return")
    public void vnPayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        try {
            quanLyPaymentService.handleVNPayReturn(params);
            response.sendRedirect("http://localhost:5173/payment-success");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/payment-failed");
        }
    }
}