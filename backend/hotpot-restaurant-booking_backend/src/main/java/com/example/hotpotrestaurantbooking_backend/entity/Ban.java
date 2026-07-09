package com.example.hotpotrestaurantbooking_backend.entity;

import com.example.hotpotrestaurantbooking_backend.enums.LoaiBan;
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
    @Column(name = "ten_ban", unique = true, nullable = false)
    private String tenBan;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "loai_ban")
    private LoaiBan loaiBan;
    @ManyToOne
    @JoinColumn(name = "id_khu_vuc")
    private KhuVuc khuVuc;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai")
    private TrangThaiBan trangThai;
}
