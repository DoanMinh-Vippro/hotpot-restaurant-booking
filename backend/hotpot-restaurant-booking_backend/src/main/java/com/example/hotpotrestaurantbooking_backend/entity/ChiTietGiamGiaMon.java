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
@Table(name = "\"ChiTietGiamGiaMon\"")
public class ChiTietGiamGiaMon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChiTietGiamGiaMon;
    @Column(name = "muc_giam")
    private BigDecimal mucGiam;

    @ManyToOne @JoinColumn(name = "id_mon")
    private Mon mon;
    @ManyToOne @JoinColumn(name = "id_dot_giam_gia")
    private DotGiamGia dotGiamGia;
}
