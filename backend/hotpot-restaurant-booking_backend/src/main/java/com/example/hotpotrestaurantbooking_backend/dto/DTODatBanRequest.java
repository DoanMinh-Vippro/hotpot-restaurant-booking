package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODatBanRequest {
        private String sdtKhachHang;
        private Integer soNguoi;
        private LocalDateTime thoiGianDenDuKien;
        private BigDecimal soTienCoc;
        private PhuongThucThanhToan phuongThucThanhToan;
        private String ghiChu;
}
