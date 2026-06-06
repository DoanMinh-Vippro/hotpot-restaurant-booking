package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOLoginResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;

public interface AuthService {
    DTOTaiKhoanResponse register(DTOTaiKhoanRequest request);

    DTOLoginResponse login(DTOTaiKhoanRequest request);
}
