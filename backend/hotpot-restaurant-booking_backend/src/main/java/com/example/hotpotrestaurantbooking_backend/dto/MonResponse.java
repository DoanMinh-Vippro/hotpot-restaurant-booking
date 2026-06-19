package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonResponse {
    private Integer idMon;
    private String tenMon;
    private BigDecimal donGiaHienTai;
    private BigDecimal giaSauGiam;
    private BigDecimal soTienDuocGiam;
    private String tenChuongTrinhGiamGia;
    private Integer idDanhMuc;
    private String loaiDanhMuc;
    private Integer trangThai;

    // Constructor dùng cho các query hiện tại trong Repository
    public MonResponse(
            Integer idMon,
            String tenMon,
            BigDecimal donGiaHienTai,
            Integer idDanhMuc,
            String loaiDanhMuc,
            Integer trangThai
    ) {
        this.idMon = idMon;
        this.tenMon = tenMon;
        this.donGiaHienTai = donGiaHienTai;

        this.idDanhMuc = idDanhMuc;
        this.loaiDanhMuc = loaiDanhMuc;
        this.trangThai = trangThai;

        // Giá trị mặc định khi chưa áp dụng giảm giá
        this.giaSauGiam = donGiaHienTai;
        this.soTienDuocGiam = BigDecimal.ZERO;
        this.tenChuongTrinhGiamGia =
                (tenChuongTrinhGiamGia == null || tenChuongTrinhGiamGia.isBlank()) ? "Không trong chương trình giảm giá" : tenChuongTrinhGiamGia;
    }
}
