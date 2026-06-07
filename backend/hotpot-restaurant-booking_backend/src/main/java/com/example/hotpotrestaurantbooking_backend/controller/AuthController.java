package org.example.datlich.controller;

import lombok.RequiredArgsConstructor;
import org.example.datlich.dto.LoginRequest;
import org.example.datlich.dto.LoginResponse;
import org.example.datlich.dto.RegisterRequest;
import org.example.datlich.dto.RegisterResponse;
import org.example.datlich.service.IAuthService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}

