package com.example.hotpotrestaurantbooking_backend.config;

import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

//    private final TaiKhoanRepository taiKhoanRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    public CommandLineRunner initData() {
//        return args -> {
//            // Tìm user 'admin'
//            Optional<TaiKhoan> adminOpt = taiKhoanRepository.findByTenDangNhap("admin");
//
//            if (adminOpt.isPresent()) {
//                TaiKhoan admin = adminOpt.get();
//                // Mã hóa đúng chuẩn BCrypt hiện tại
//                String encodedPassword = passwordEncoder.encode("123456");
//                admin.setMatKhau(encodedPassword);
//
//                taiKhoanRepository.save(admin);
//
//                System.out.println("==========================================");
//                System.out.println("ĐÃ CẬP NHẬT MẬT KHẨU MỚI CHO ADMIN");
//                System.out.println("Mật khẩu mã hóa: " + encodedPassword);
//                System.out.println("==========================================");
//            }
//        };
//    }
}