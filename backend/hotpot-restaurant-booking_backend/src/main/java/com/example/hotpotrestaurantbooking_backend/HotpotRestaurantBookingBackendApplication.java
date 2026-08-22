package com.example.hotpotrestaurantbooking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableAsync
@SpringBootApplication
@EnableScheduling
public class HotpotRestaurantBookingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotpotRestaurantBookingBackendApplication.class, args);
        System.out.println("Success");
    }

}
