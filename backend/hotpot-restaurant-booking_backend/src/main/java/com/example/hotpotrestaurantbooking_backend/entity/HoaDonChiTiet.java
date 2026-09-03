package com.example.hotpotrestaurantbooking_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"HoaDonChiTiet\"")
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHoaDonChiTiet;
    private String maHoaDonChiTiet;
    private Integer soLuong;
    @Column(name = "da_len")
    private Integer daLen = 0;

    @Column(name = "trang_thai_mon_an")
    private String trangThaiMonAn;
    private BigDecimal giaBanTaiThoiDien;
    private BigDecimal tienGiamGiaMon;
    private BigDecimal thanhTien;

    @Transient
    private LocalDateTime orderedAt;

    @Transient
    private String orderedBy;

    @ManyToOne @JoinColumn(name = "id_mon") private Mon mon;
    @ManyToOne @JoinColumn(name = "id_combo") private Combo combo;
    @ManyToOne @JoinColumn(name = "id_hoa_don") private HoaDon hoaDon;
}
