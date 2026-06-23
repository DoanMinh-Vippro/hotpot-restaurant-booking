package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOLoginResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTORegisterRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangResponse;

public interface AuthService {
    DTOTaiKhoanResponse register(DTOTaiKhoanRequest request);

    KhachHangResponse registerCustomer(DTORegisterRequest request);

    DTOLoginResponse login(DTOTaiKhoanRequest request);
}
