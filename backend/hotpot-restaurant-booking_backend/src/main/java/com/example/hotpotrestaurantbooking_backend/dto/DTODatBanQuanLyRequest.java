package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotEmpty(message = "Vui lòng chọn ít nhất một bàn")
    private List<Integer> dsBan;

    private List<DTOChiTietDatBanComboRequest> dsCombo;

    private Integer idKhachHang;

    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String sdtKhachHang;

    @NotNull(message = "Số người không được để trống")
    @Min(value = 1, message = "Số người phải lớn hơn 0")
    private Integer soNguoi;

    private String ghiChu;

    @NotNull(message = "Vui lòng chọn thời gian đến")
    @Future(message = "Thời gian đến phải sau thời điểm hiện tại")
    private LocalDateTime thoiGianDenDuKien;

    private BigDecimal soTienCoc;

    private TrangThaiDatBanCoc trangThaiCoc;

    private PhuongThucThanhToan phuongThucThanhToan;

    private TrangThaiDatBan trangThai;
}