package com.example.hotpotrestaurantbooking_backend.entity;

import com.example.hotpotrestaurantbooking_backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentIntent {

    @Id
    @GeneratedValue
    private Long id;

    private Integer idDatBan;

    private Integer amount;

    private String content; // DATBAN_12

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;
}
