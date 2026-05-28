package com.example.hotpotrestaurantbooking_backend.entity;

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
@Table(name = "\"DatBan\"")
public class DatBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDatBan;
    @ManyToOne
    @JoinColumn(name = "id_ban",referencedColumnName = "id_ban")
    private Ban ban;
    @ManyToOne
    @JoinColumn(name = "id_khach_hang",referencedColumnName = "id_khach_hang")
    private KhachHang khachHang;
    private LocalDate ngayDat;
    private LocalTime gioDat;
    private String sdtKhachHang;
    private int soNguoi;
    private int trangThai;
    private String ghiChu;
    private LocalDateTime thoiGianDenDuKien;
    private BigDecimal soTienCoc;
    private int trangThaiCoc;
    private int phuongThucThanhToan;
}
