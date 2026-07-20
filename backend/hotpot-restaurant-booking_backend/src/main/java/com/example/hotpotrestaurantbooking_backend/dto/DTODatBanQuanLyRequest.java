package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

    @Size(max = 100, message = "Tên khách hàng tối đa 100 ký tự")
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String sdtKhachHang;

    @NotNull(message = "Số người không được để trống")
    @Min(value = 1, message = "Số người phải lớn hơn 0")
    private Integer soNguoi;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    @NotNull(message = "Vui lòng chọn thời gian đến")
    @Future(message = "Thời gian đến phải sau thời điểm hiện tại")
    private LocalDateTime thoiGianDenDuKien;

    @DecimalMin(value = "0", inclusive = true, message = "Tiền cọc không được âm")
    private BigDecimal soTienCoc;

    private TrangThaiDatBanCoc trangThaiCoc;

    private PhuongThucThanhToan phuongThucThanhToan;

    private TrangThaiDatBan trangThai;
}