package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ChiTietDatBanCombo")
public class ChiTietDatBanCombo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet_dat_ban_combo")
    private Integer idChiTietDatBanCombo;

    @ManyToOne
    @JoinColumn(name = "id_dat_ban")
    private DatBan datBan;

    @ManyToOne
    @JoinColumn(name = "id_combo")
    private Combo combo;

    @Column(name = "so_luong")
    private Integer soLuong;
}