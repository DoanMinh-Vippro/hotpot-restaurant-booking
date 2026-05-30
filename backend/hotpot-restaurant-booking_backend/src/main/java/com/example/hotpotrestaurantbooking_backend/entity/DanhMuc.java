package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"DanhMuc\"")
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDanhMuc;
    @Column(name = "loai_danh_muc")
    private String loaiDanhMuc;
    @Column(name = "mo_ta")
    private String moTa;
}
