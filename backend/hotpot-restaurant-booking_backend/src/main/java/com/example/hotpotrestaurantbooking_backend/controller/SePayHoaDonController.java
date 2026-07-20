package com.example.hotpotrestaurantbooking_backend.controller;
import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.service.SePayHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sepay-hoadon/webhook")
@RequiredArgsConstructor
public class SePayHoaDonController {
    private final SePayHoaDonService sePayHoaDonService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody DTOSepayWebhook payload) {
        System.out.println("Nhận tín hiệu giao dịch SePay cho hóa đơn: " + payload.getContent());

        sePayHoaDonService.handleHoaDonWebhook(payload);

        return ResponseEntity.ok().build();
    }
}
