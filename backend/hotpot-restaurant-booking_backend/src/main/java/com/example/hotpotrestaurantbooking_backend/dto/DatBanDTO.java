package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: DatBanDTO
// ==========================

import lombok.*;

import java.math.BigDecimal;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatBanDTO {

    private Integer idDatBan;

    private Integer idBan;

    private Integer idKhachHang;

    private LocalDate ngayDat;

    private LocalTime gioDat;

    private String sdtKhachHang;

    private Integer soNguoi;

    private Integer trangThai;

    private String ghiChu;

    private LocalDateTime thoiGianDenDuKien;

    private BigDecimal soTienCoc;

    private Integer trangThaiCoc;

    private Integer phuongThucThanhToan;
}