package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.NotificationResponse;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/thong-bao")
public class NotificationController {
    private final NotificationService notificationService;
    private final KhachHangRepository khachHangRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return ResponseEntity.ok(List.of());
        }

        Integer accountId = ((Number) jwt.getClaim("idTaiKhoan")).intValue();
        KhachHang customer = khachHangRepository.findByTaiKhoan_IdTaiKhoan(accountId).orElse(null);
        if (customer != null) {
            return ResponseEntity.ok(notificationService.getForCustomer(customer.getIdKhachHang()));
        }
        return ResponseEntity.ok(notificationService.getForStaff(accountId));
    }
}