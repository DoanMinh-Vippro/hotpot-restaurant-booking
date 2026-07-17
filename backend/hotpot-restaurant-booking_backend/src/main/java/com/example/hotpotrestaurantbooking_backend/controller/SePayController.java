package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.service.PaymentService;
import com.example.hotpotrestaurantbooking_backend.service.QuanLyPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sepay/webhook")
@RequiredArgsConstructor
public class SePayController {
    private final PaymentService paymentService;
    private final QuanLyPaymentService quanLyPaymentService;

    @PostMapping
    public void webhook(@RequestBody DTOSepayWebhook payload) {

        System.out.println("WEBHOOK HIT: " + payload.getContent());

        if (payload.getContent().startsWith("QLDATBAN_")) {
            quanLyPaymentService.handleWebhook(payload);
        } else {
            paymentService.handleWebhook(payload);
        }
    }
}