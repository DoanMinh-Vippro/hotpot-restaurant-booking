package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"KhuVuc\"")
public class KhuVuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_khu_vuc") // Khai báo rõ tên cột trong DB là id_khu_vuc
    private Integer idKhuVuc;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ten_khu_vuc")
    private String tenKhuVuc;

    @Column(name = "trang_thai")
    private int trangThai;
}
