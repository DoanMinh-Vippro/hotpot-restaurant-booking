package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOPaymentResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.dto.DTOVNPayResponse;

import java.util.Map;

public interface PaymentService {

    DTOPaymentResponse createPayment(DTODatBanRequest dto);

    void handleWebhook(DTOSepayWebhook payload);
    boolean checkPaymentStatus(String content);
    DTOVNPayResponse createVNPayPayment(DTODatBanRequest dto);
    void handleVNPayReturn(Map<String, String> params);
}