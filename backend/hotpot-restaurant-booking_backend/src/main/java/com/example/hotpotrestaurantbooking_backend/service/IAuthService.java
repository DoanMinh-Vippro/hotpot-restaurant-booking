package org.example.datlich.service;

import org.example.datlich.dto.LoginRequest;
import org.example.datlich.dto.LoginResponse;
import org.example.datlich.dto.RegisterRequest;
import org.example.datlich.dto.RegisterResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);

}
