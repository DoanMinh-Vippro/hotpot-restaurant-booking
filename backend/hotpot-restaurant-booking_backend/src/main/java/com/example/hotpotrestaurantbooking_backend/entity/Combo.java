package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"Combo\"")
public class Combo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCombo;
    @Column(name = "ten_combo")
    private String tenCombo;
    @Column(name = "gia_combo")
    private BigDecimal giaCombo;
    @Column(name = "hinh_anh")
    private String hinhAnh;
    @Column(name = "trang_thai")
    private Integer trangThai;
}