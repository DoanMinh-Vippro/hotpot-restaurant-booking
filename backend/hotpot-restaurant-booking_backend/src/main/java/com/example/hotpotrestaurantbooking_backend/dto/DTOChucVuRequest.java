package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOChucVuRequest {
    private String maChucVu;
    private String tenChucVu;
}
