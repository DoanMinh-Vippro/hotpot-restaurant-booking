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

@Service // Rất quan trọng để Spring nhận diện đây là một Bean
@RequiredArgsConstructor // Tự động inject repository thông qua constructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm tài khoản trong database
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));


        String chucVu = (tk.getChucVu() != null) ? tk.getChucVu().getTenChucVu() : "USER"; // Mặc định là USER nếu không có chức vụ
        // 2. Chuyển đổi entity TaiKhoan sang UserDetails của Spring Security
        return new User(
                tk.getTenDangNhap(),
                tk.getMatKhau(),
                tk.getTrangThai(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                // Chuyển đổi quyền từ DB thành dạng Spring Security hiểu
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + tk.getChucVu().getTenChucVu().toUpperCase()))
        );
    }
}