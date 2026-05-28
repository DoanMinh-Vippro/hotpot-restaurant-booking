package com.example.hotpotrestaurantbooking_backend.entity;

import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
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
@Table(name = "Ban")
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBan;
    private String loaiBan;
    private int soLuongBan;
    @ManyToOne
    @JoinColumn(name = "id_khu_vuc")
    private KhuVuc khuVuc;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai")
    private TrangThaiBan trangThai;
}
