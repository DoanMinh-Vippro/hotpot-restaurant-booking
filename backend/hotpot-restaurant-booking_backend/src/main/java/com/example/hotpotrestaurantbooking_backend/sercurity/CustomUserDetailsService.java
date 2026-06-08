package com.example.hotpotrestaurantbooking_backend.sercurity;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        // CHỐNG LỖI NULL AN TOÀN:
        // 1. Kiểm tra tk.getChucVu() có tồn tại không
        // 2. Kiểm tra tên chức vụ có null không
        String tenChucVu = (tk.getChucVu() != null && tk.getChucVu().getTenChucVu() != null)
                ? tk.getChucVu().getTenChucVu().toUpperCase()
                : "USER";

        // Tạo authority (Luôn có ROLE_ prefix)
        String role = tenChucVu.startsWith("ROLE_") ? tenChucVu : "ROLE_" + tenChucVu;

        return new User(
                tk.getTenDangNhap(),
                tk.getMatKhau(),
                tk.getTrangThai(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}