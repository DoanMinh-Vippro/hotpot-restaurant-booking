package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"GiamGia\"")
public class GiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
