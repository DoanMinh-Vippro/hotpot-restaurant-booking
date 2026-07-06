package com.example.hotpotrestaurantbooking_backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnp.tmnCode}")
    private String tmnCode;

    @Value("${vnp.hashSecret}")
    private String hashSecret;

    @Value("${vnp.payUrl}")
    private String payUrl;

    @Value("${vnp.returnUrl}")
    private String returnUrl;

}