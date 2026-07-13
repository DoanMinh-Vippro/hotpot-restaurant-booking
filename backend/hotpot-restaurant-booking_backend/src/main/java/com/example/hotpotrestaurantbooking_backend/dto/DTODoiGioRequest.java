package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODoiGioRequest {

    @NotNull(message = "Vui lòng chọn thời gian mới")
    @Future(message = "Thời gian phải ở tương lai")
    private LocalDateTime thoiGianMoi;

}
    