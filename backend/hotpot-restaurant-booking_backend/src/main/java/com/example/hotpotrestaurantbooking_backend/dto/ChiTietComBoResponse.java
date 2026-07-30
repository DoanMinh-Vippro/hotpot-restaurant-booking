package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietComBoResponse {
    private Integer idChiTietCombo;
    private Integer soLuong;
    private Integer idMon;
    private String tenMon;
    private Integer idCombo;
    private String tenCombo;
    private BigDecimal giaCombo;
    private String hinhAnh;
    private Integer trangThai;
    private String moTa;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonInComboDTO {
        private Integer idMon;
        private String tenMon;
        private Integer soLuong;
    }

    // Danh sách món ăn thuộc Combo này
    private List<MonInComboDTO> danhSachMon;

    // Constructor dùng cho Query JPQL phẳng cũ
    public ChiTietComBoResponse(Integer idChiTietCombo, Integer soLuong, Integer idMon, String tenMon,
                                Integer idCombo, String tenCombo, BigDecimal giaCombo,
                                String hinhAnh, Integer trangThai, String moTa) {
        this.idChiTietCombo = idChiTietCombo;
        this.soLuong = soLuong;
        this.idMon = idMon;
        this.tenMon = tenMon;
        this.idCombo = idCombo;
        this.tenCombo = tenCombo;
        this.giaCombo = giaCombo;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.moTa = moTa;
    }
}
