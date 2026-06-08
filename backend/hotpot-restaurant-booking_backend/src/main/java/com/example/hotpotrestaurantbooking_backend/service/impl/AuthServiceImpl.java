package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOLoginResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder; // Tiêm JwtEncoder vào

    @Override
    public DTOTaiKhoanResponse register(DTOTaiKhoanRequest request) {
        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())){
            throw new IllegalArgumentException("tên đăng nhập đã tồn tại");
        }
        TaiKhoan tk = mapper.map(request, TaiKhoan.class);
        // Mã hóa mật khẩu trước khi lưu
        tk.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        tk.setTrangThai(true);
        taiKhoanRepository.save(tk);

        tk.setMaTaiKhoan(String.format("TK%03d", tk.getIdTaiKhoan()));
        taiKhoanRepository.save(tk);
        return mapper.map(tk, DTOTaiKhoanResponse.class);
    }

    @Override
    public DTOLoginResponse login(DTOTaiKhoanRequest request) {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(request.getTenDangNhap())
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu"));

        // Log kiểm tra
        boolean isMatch = passwordEncoder.matches(request.getMatKhau(), tk.getMatKhau());
        if (!isMatch) {
            System.out.println("DEBUG: Mật khẩu input: " + request.getMatKhau());
            System.out.println("DEBUG: Mật khẩu trong DB: " + tk.getMatKhau());
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }

        if (!tk.getTrangThai()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        return new DTOLoginResponse(createToken(tk));
    }

    private String createToken(TaiKhoan tk) {
        Instant now = Instant.now();
        // Lấy chức vụ từ đối tượng tk
        String chucVu = (tk.getChucVu() != null) ? tk.getChucVu().getTenChucVu() : "USER";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(tk.getTenDangNhap())
                // Nhúng quyền vào claim "scope" để Controller sử dụng
                .claim("scope", "ROLE_" + chucVu.toUpperCase())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

