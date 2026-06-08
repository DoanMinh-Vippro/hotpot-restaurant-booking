package com.example.hotpotrestaurantbooking_backend.dto;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODatBanRequest {
        @NotBlank(message = "không được b trống số điện thoại")
        @Pattern(regexp = "^[0][1-9][0-9]{8}$", message = "Số điện thoại không đúng định dạng (VD: 0987654321)")
        private String sdtKhachHang;
        @NotNull(message = "số người không được bỏ trống")
        private Integer soNguoi;
        @FutureOrPresent(message = "Không được chọn ngày trong quá khứ")
        private LocalDateTime thoiGianDenDuKien;
        private BigDecimal soTienCoc;
        @NotNull(message = "Hãy chọn phương thức thanh toán")
        private PhuongThucThanhToan phuongThucThanhToan;
        private String ghiChu;
}
