package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"TaiKhoan\"")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTaiKhoan;
    @Column(unique = true)
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private Boolean trangThai;
}
