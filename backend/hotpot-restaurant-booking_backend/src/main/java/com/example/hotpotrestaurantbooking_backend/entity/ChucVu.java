package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
}
