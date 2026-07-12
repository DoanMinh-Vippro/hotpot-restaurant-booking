package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.LoaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODatBanQuanLyRequest {

    private Integer idBan;

    private List<DTOChiTietDatBanComboRequest> dsCombo;

    private LoaiBan loaiBan;

    private Integer idKhachHang;

    private String sdtKhachHang;

    private int soNguoi;

    private TrangThaiDatBan trangThai;

    private String ghiChu;

    private LocalDateTime thoiGianDenDuKien;

    private BigDecimal soTienCoc;

    private TrangThaiDatBanCoc trangThaiCoc;

    private PhuongThucThanhToan phuongThucThanhToan;
}