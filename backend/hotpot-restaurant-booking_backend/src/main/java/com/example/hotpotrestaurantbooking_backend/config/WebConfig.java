package com.example.hotpotrestaurantbooking_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để khi web gọi /uploads/abc.jpg thì Spring Boot sẽ vào thư mục uploads/ ở ổ cứng để lấy file
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}