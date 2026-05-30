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
@Table(name = "\"Mon\"")
public class Mon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMon;
    @Column(name = "ten_mon")
    private String tenMon;
    @Column(name = "don_gia_hien_tai")
    private BigDecimal donGiaHienTai;
    @Column(name = "trang_thai")
    private Integer trangThai;

    @ManyToOne @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;
}