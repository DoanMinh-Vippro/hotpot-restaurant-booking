package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOPaymentResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.dto.DTOVNPayResponse;

import java.util.Map;

public interface QuanLyPaymentService {

    DTOPaymentResponse createPayment(DTODatBanQuanLyRequest dto);

    void handleWebhook(DTOSepayWebhook payload);

    boolean checkPaymentStatus(String content);

    DTOVNPayResponse createVNPayPayment(DTODatBanQuanLyRequest dto);

    void handleVNPayReturn(Map<String, String> params);

}