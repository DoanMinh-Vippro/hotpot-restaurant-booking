package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BanRequest {
    @NotBlank(message = "Loại bàn không được để trống")
    private String loaiBan;

    @NotNull(message = "Số lượng bàn không được để trống")
    private Integer soLuongBan;

    private Integer trangThai;

    @NotNull(message = "Khu vực không được để trống")
    private Integer khuVucId;
}
