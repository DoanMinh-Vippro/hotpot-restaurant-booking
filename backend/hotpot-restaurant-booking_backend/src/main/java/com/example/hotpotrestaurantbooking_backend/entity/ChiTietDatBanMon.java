package com.example.hotpotrestaurantbooking_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "ChiTietDatBanMon",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_dat_ban", "id_mon"})
)
public class ChiTietDatBanMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet_dat_ban_mon")
    private Integer idChiTietDatBanMon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dat_ban", nullable = false)
    @JsonBackReference
    private DatBan datBan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mon", nullable = false)
    private Mon mon;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}