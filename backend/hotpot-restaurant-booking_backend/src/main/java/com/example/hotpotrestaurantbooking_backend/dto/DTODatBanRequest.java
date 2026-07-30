package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
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
public class DTODatBanRequest {

        @NotBlank(message = "không được b trống số điện thoại")
        @Pattern(regexp = "^(03|05|07|08|09)\\d{8}$", message = "Số điện thoại không đúng định dạng")
        private String sdtKhachHang;


        private List<DTOChiTietDatBanComboRequest> dsCombo;
        private List<DTOChiTietDatBanMonRequest> dsMon;
        private List<Integer> dsBan;

        @NotNull(message = "số người không được bỏ trống")
        @Min(value = 1, message = "số người phải > 0")
        private Integer soNguoi;

        @FutureOrPresent(message = "Không được chọn ngày trong quá khứ")
        private LocalDateTime thoiGianDenDuKien;

        @PositiveOrZero(message = "Tiền cọc không hợp lệ")
        private BigDecimal soTienCoc;

        @NotNull(message = "Hãy chọn phương thức thanh toán")
        private PhuongThucThanhToan phuongThucThanhToan;

        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        private String ghiChu;
}