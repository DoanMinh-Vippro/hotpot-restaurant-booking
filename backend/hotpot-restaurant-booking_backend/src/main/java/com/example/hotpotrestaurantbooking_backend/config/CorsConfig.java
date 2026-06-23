package com.example.hotpotrestaurantbooking_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả các đường dẫn API
                        .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*") // Cho phép localhost
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Các phương thức được phép
                        .allowedHeaders("*") // Cho phép tất cả các header
                        .allowCredentials(true) // Cho phép gửi credentials (cookies, auth headers)
                        .maxAge(3600); // Cache CORS trong 1 giờ
            }
        };
    }
}
