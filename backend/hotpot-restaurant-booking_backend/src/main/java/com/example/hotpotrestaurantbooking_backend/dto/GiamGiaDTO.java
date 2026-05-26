package com.example.hotpotrestaurantbooking_backend.dto;
// ==========================
// DTO: GiamGiaDTO
// ==========================


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiamGiaDTO {

    private Integer idGiamGia;

    private String maGiamGia;

    private LocalDate ngayTao;

    private LocalDate ngayKetThuc;

    private String dieuKienSuDung;

    private BigDecimal giaTriGiamToiDa;

    private BigDecimal giaTriGiam;

    private String loaiGiam;

    private Integer soLuongMaGiamGia;

    private Integer soLuongDung;

    private Integer trangThai;
}