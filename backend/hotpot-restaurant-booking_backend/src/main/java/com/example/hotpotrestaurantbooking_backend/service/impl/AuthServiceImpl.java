package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOLoginResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTORegisterRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final ChucVuRepository chucVuRepository;
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
    public KhachHangResponse registerCustomer(DTORegisterRequest request) {
        // Kiểm tra tên đăng nhập đã tồn tại
        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())){
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký");
        }

        // Tạo TaiKhoan mới với role USER (id = 3)
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(request.getTenDangNhap());
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        taiKhoan.setTrangThai(true);

        // Set role USER (id = 3)
        ChucVu roleUser = chucVuRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));
        taiKhoan.setChucVu(roleUser);

        // Lưu TaiKhoan
        taiKhoanRepository.save(taiKhoan);

        // Tạo mã tài khoản
        taiKhoan.setMaTaiKhoan(String.format("TK%03d", taiKhoan.getIdTaiKhoan()));
        taiKhoanRepository.save(taiKhoan);

        // Tạo KhachHang mới
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang("KH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        khachHang.setTenKhachHang(request.getTenKhachHang());
        khachHang.setGioiTinh(request.getGioiTinh());
        khachHang.setSoDienThoai(request.getSoDienThoai());
        khachHang.setEmail(request.getEmail());
        khachHang.setDiaChi(request.getDiaChi());
        khachHang.setTrangThai(true);
        khachHang.setTaiKhoan(taiKhoan);

        // Lưu KhachHang
        khachHangRepository.save(khachHang);

        // Trả về response
        return mapper.map(khachHang, KhachHangResponse.class);
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

        String role = (tk.getChucVu() != null) ? tk.getChucVu().getTenChucVu() : "USER";
        String token = createToken(tk);
        
        // Lấy thông tin khách hàng nếu là user
        DTOLoginResponse response = new DTOLoginResponse();
        response.setToken(token);
        response.setRole(role);
        
        if ("USER".equalsIgnoreCase(role)) {
            KhachHang kh = khachHangRepository.findByTaiKhoan(tk);
            if (kh != null) {
                response.setKhachHangId(kh.getIdKhachHang());
                response.setTenKhachHang(kh.getTenKhachHang());
                response.setSoDienThoai(kh.getSoDienThoai());
                response.setEmail(kh.getEmail());
                response.setDiaChi(kh.getDiaChi());
                response.setGioiTinh(kh.getGioiTinh());
                response.setMaKhachHang(kh.getMaKhachHang());
            }
        }
        
        return response;
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
                .claim("idTaiKhoan", tk.getIdTaiKhoan())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

