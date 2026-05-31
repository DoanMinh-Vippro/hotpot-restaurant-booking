package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODatBanResponse {
    private Integer idDatBan;
    private LocalDate ngayDat;
    private LocalTime gioDat;
    private String sdtKhachHang;
    private TrangThaiDatBan trangThai;
    private int soNguoi;
    private String ghiChu;
    private LocalDateTime thoiGianDenDuKien;
    private BigDecimal soTienCoc;
    private PhuongThucThanhToan phuongThucThanhToan;
}
