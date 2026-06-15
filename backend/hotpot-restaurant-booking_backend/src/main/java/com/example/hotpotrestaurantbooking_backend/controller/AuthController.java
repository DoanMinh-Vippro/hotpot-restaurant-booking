package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOLoginResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTORegisterRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangResponse;
import com.example.hotpotrestaurantbooking_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<DTOTaiKhoanResponse> register(@Valid @RequestBody DTOTaiKhoanRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("register-customer")
    public ResponseEntity<KhachHangResponse> registerCustomer(@Valid @RequestBody DTORegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCustomer(request));
    }

    @PostMapping("login")
    public ResponseEntity<DTOLoginResponse> login(@Valid @RequestBody DTOTaiKhoanRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}
