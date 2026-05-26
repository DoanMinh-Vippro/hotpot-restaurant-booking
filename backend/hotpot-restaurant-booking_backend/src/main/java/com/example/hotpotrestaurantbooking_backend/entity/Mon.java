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
@Table(name = "Mon")
public class Mon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMon;
    private String tenMon;
    private BigDecimal donGiaHienTai;

    @ManyToOne @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;
}