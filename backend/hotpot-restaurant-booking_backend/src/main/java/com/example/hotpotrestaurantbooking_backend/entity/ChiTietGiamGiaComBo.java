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
@Table(name = "ChiTietGiamGiaCombo")
public class ChiTietGiamGiaComBo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChiTietGiamGiaCombo;

    @Column(name = "muc_giam")
    private BigDecimal mucGiam;

    @Column(name = "loai_giam")
    private String loaiGiam;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @ManyToOne
    @JoinColumn(name = "id_combo")
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia")
    private DotGiamGia dotGiamGia;
}
