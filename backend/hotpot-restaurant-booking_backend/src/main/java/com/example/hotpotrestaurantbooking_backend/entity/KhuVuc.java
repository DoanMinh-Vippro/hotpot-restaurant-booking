package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"KhuVuc\"")
public class KhuVuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_khu_vuc")
    private Integer idKhuVuc;

    @Column(name = "ten_khu_vuc", length = 30)
    private String tenKhuVuc;

    @Column(name = "mo_ta", length = 100)
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;  // 0: Không hoạt động, 1: Hoạt động

    @OneToMany(mappedBy = "khuVuc", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Ban> banList;

}
