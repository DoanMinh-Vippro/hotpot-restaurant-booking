package com.example.hotpotrestaurantbooking_backend.service;


import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;

public interface SePayHoaDonService {
    void handleHoaDonWebhook(DTOSepayWebhook payload);
}
