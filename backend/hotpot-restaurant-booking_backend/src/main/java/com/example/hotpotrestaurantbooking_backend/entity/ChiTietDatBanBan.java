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
@Table(name = "ChiTietDatBanBan")
public class ChiTietDatBanBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet_dat_ban_ban")
    private Integer idChiTietDatBanBan;

    @ManyToOne
    @JoinColumn(name = "id_dat_ban")
    @JsonBackReference
    private DatBan datBan;

    @ManyToOne
    @JoinColumn(name = "id_ban")
    private Ban ban;
}