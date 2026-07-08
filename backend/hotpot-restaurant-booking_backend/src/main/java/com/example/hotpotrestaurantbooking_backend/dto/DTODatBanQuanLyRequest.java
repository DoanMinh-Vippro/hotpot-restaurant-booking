package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.LoaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "số người không được để trống")
    private int soNguoi;

    @NotNull(message = "trạng thái bàn không được để trống")
    private TrangThaiDatBan trangThai;

    private String ghiChu;

    @FutureOrPresent(message = "không được để ngày trong quá khứ")
    private LocalDateTime thoiGianDenDuKien;

    private BigDecimal soTienCoc;

    private TrangThaiDatBanCoc trangThaiCoc;

    @NotNull(message = "không được để trống phương thức thanh toán")
    private PhuongThucThanhToan phuongThucThanhToan;
}