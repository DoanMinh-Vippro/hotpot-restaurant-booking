package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sepay/webhook")
@RequiredArgsConstructor
public class SePayController {

    private final PaymentService paymentService;

    @PostMapping
    public void webhook(@RequestBody DTOSepayWebhook payload) {

        System.out.println("WEBHOOK HIT: " + payload.getContent());

        paymentService.handleWebhook(payload);
    }
}