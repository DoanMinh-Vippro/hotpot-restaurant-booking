package com.example.hotpotrestaurantbooking_backend.dto;

// ==========================
// DTO: ComboDTO
// ==========================

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboResponse {

    private Integer idCombo;
    private String tenCombo;
    private BigDecimal giaCombo;
    private String hinhAnh;
    private Integer trangThai;
    private Integer trangThaiBan;

    // Các trường động do TinhTienImpl tính toán
    private BigDecimal giaSauGiam;
    private BigDecimal soTienDuocGiam;
    private String tenChuongTrinhGiamGia;

    // Constructor dùng cho các JPQL Query trong ComboRepository
    public ComboResponse(
            Integer idCombo,
            String tenCombo,
            BigDecimal giaCombo,
            String hinhAnh,
            Integer trangThai,
            Integer trangThaiBan
    ) {
        this.idCombo = idCombo;
        this.tenCombo = tenCombo;
        this.giaCombo = giaCombo;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.trangThaiBan = trangThaiBan;

        // Giá trị mặc định khi chưa qua TinhTienImpl
        this.giaSauGiam = giaCombo;
        this.soTienDuocGiam = BigDecimal.ZERO;
        this.tenChuongTrinhGiamGia = "Không trong chương trình giảm giá";
    }
}