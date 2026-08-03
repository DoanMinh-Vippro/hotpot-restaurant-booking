package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.MayInRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/print")
public class PrintController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send-ticket")
    public ResponseEntity<?> sendToVirtualScreen(@RequestBody MayInRequest request) {
        try {
            if (request.getTenQuay() != null && request.getTenQuay().toUpperCase().contains("BAR")) {
                messagingTemplate.convertAndSend("/topic/bar", request);
            } else {
                messagingTemplate.convertAndSend("/topic/bep", request);
            }

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Đã gửi tới màn hình!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}