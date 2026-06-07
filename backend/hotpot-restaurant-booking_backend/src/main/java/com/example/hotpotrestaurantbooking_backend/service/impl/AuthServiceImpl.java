package org.example.datlich.impl;

import lombok.RequiredArgsConstructor;
import org.example.datlich.dto.LoginRequest;
import org.example.datlich.dto.LoginResponse;
import org.example.datlich.dto.RegisterRequest;
import org.example.datlich.dto.RegisterResponse;
import org.example.datlich.entity.ChucVu;
import org.example.datlich.entity.NhanVien;
import org.example.datlich.entity.TaiKhoan;
import org.example.datlich.repository.ChucVuRepository;
import org.example.datlich.repository.NhanVienRepository;
import org.example.datlich.repository.TaiKhoanRespository;
import org.example.datlich.service.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final TaiKhoanRespository taiKhoanRepo;
    private final NhanVienRepository nhanVienRepo;
    private final ChucVuRepository chucVuRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = generateToken(authentication);

        return LoginResponse.builder()
                .token(token)
                .username(authentication.getName())
                .role(authentication.getAuthorities().iterator().next().getAuthority())
                .build();
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (taiKhoanRepo.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        TaiKhoan tk = TaiKhoan.builder()
                .maTaiKhoan("TK" + System.currentTimeMillis())
                .tenDangNhap(request.getTenDangNhap())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .trangThai(true)
                .build();

        taiKhoanRepo.save(tk);

        ChucVu chucVu = chucVuRepo.findById(request.getIdChucVu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));

        NhanVien nv = NhanVien.builder()
                .maNhanVien("NV" + System.currentTimeMillis())
                .tenNhanVien(request.getTenNhanVien())
                .gioiTinh(request.getGioiTinh())
                .soDienThoai(request.getSoDienThoai())
                .email(request.getEmail())
                .diaChi(request.getDiaChi())
                .trangThai(true)
                .taiKhoan(tk)
                .chucVu(chucVu)
                .build();

        nhanVienRepo.save(nv);

        return RegisterResponse.builder()
                .username(tk.getTenDangNhap())
                .role(chucVu.getTen_chuc_vu())
                .message("Đăng ký thành công")
                .build();
    }

    private String generateToken(Authentication authentication) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("datlich-api")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(authentication.getName())
                .claim("scope", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" ")))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
