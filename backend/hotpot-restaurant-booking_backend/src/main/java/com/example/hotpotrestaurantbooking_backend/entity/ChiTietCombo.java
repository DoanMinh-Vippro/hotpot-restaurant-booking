package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ChiTietCombo")
public class ChiTietCombo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChiTietCombo;
    @Column(name = "so_luong")
    private Integer soLuong;
    @Column(name = "mo_ta")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "id_mon")
    private Mon mon;
    @ManyToOne
    @JoinColumn(name = "id_combo")
    private Combo combo;
}
