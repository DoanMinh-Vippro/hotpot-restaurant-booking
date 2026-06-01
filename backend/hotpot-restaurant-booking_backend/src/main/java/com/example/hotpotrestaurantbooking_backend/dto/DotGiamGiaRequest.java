    package com.example.hotpotrestaurantbooking_backend.dto;

    // ==========================
    // DTO: DotGiamGiaDTO
    // ==========================


    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class DotGiamGiaRequest {

        private Integer idDotGiamGia;
        @NotBlank(message = "Tên chương trình không được để trống")
        @Size(min = 3, max = 50,
                message = "Tên chương trình phải từ 3 đến 50 ký tự")
        private String tenChuongTrinh;
        @NotNull(message = "Ngày bắt đầu không được để trống")
        private LocalDate ngayBatDau;
        @NotNull(message = "Ngày kết thúc không được để trống")
        private LocalDate ngayKetThuc;
    }