package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"DotGiamGia\"")
public class DotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDotGiamGia;
    @Column(name = "ten_chuong_trinh")
    private String tenChuongTrinh;
    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;
    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;
}
