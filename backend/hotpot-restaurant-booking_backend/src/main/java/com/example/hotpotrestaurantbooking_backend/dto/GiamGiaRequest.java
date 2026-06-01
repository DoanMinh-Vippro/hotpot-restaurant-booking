package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GiamGiaRequest {
    private Integer idGiamGia;

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(min = 3, max = 40, message = "Mã giảm giá phải từ 3 đến 40 ký tự")
    private String maGiamGia;

    private LocalDate ngayTao;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    @NotBlank(message = "Điều kiện sử dụng không được để trống")
    @Size(min = 5, max = 255, message = "Điều kiện sử dụng phải từ 5 đến 255 ký tự")
    private String dieuKienSuDung;

    @NotNull(message = "Giá trị giảm tối đa không được để trống")
    @DecimalMin(value = "0.01", inclusive = true, message = "Giá trị giảm tối đa phải lớn hơn 0")
    private BigDecimal giaTriGiamToiDa;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", inclusive = true, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    @NotBlank(message = "Loại giảm không được để trống")
    @Size(min = 3, max = 40, message = "Loại giảm phải từ 3 đến 40 ký tự")
    private String loaiGiam;

    @NotNull(message = "Số lượng mã giảm giá không được để trống")
    @Positive(message = "Số lượng mã giảm giá phải lớn hơn 0")
    private Integer soLuongMaGiamGia;

    private Integer soLuongDung;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
