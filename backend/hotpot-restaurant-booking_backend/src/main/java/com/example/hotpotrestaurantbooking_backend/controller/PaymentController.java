package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOPaymentResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOVNPayResponse;
import com.example.hotpotrestaurantbooking_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public DTOPaymentResponse createPayment(@RequestBody DTODatBanRequest dto) {
        return paymentService.createPayment(dto);
    }

    @GetMapping("/status")
    public boolean checkPaymentStatus(@RequestParam String content) {
        return paymentService.checkPaymentStatus(content);
    }

    @PostMapping("/vnpay/create")
    public ResponseEntity<DTOVNPayResponse> createVNPay(@RequestBody @Valid DTODatBanRequest dto) {
        return ResponseEntity.ok(paymentService.createVNPayPayment(dto));
    }


    @GetMapping("/vnpay-return")
    public void vnPayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {

        try {
            paymentService.handleVNPayReturn(params);
            response.sendRedirect("http://localhost:5173/payment-success");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/payment-failed");
        }
    }

}