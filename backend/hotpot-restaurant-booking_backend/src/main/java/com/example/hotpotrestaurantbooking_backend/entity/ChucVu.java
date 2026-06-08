package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
<<<<<<< HEAD
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ChucVu")
public class ChucVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChucVu;
    private String maChucVu;
    private String tenChucVu;
=======
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChucVu")
@Builder
public class ChucVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chuc_vu")
    private Integer id;
    @Column(name = "ma_chuc_vu")
    private String ma_chuc_vu;
    @Column(name = "ten_chuc_vu")
    private String ten_chuc_vu;
>>>>>>> 6741c3e41b67912050083fa6b80f7e4b3d98e35f
}
