package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.NotificationResponse;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;

import java.util.List;

public interface NotificationService {
    void notifyCustomer(Integer customerId, String eventKey, String title, String message);

    void notifyCustomer(Integer customerId, String phone, String eventKey, String title, String message);

    void notifyStaff(String eventKey, String title, String message, List<NhanVien> staff);

    List<NotificationResponse> getForCustomer(Integer customerId);

    List<NotificationResponse> getForCustomer(Integer customerId, String phone);

    List<NotificationResponse> getForStaff(Integer accountId);
}