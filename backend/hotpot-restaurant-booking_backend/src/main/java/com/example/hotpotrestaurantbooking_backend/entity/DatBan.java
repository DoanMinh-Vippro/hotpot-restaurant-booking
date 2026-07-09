package com.example.hotpotrestaurantbooking_backend.entity;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DatBan")
public class DatBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dat_ban")
    private Integer idDatBan;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @OneToMany(mappedBy = "datBan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChiTietDatBanCombo> chiTietDatBanCombos;

    @OneToMany(mappedBy = "datBan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChiTietDatBanBan> chiTietDatBanBans;

    private LocalDate ngayDat;

    private Time gioDat;

    private String sdtKhachHang;

    private int soNguoi;

    @Enumerated(EnumType.ORDINAL)
    private TrangThaiDatBan trangThai;

    private String ghiChu;

    private LocalDateTime thoiGianDenDuKien;

    private BigDecimal soTienCoc;

    @Enumerated(EnumType.ORDINAL)
    private TrangThaiDatBanCoc trangThaiCoc;

    @Enumerated(EnumType.ORDINAL)
    private PhuongThucThanhToan phuongThucThanhToan;
}