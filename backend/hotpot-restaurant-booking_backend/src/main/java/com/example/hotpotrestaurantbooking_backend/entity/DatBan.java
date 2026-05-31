package com.example.hotpotrestaurantbooking_backend.entity;

import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    @JoinColumn(name = "id_ban")
    private Ban ban;
    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
    private LocalDate ngayDat;
    private LocalTime gioDat;
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
