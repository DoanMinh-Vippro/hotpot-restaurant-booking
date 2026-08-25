package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOHoaDonRequest {
    @Size(max = 50, message = "Mã hóa đơn không được vượt quá 50 ký tự")
    private String maHoaDon;
    private String maGiaoDich;
    private Integer trangThaiHoaDon;
    private String sdtKhachHang;
    private BigDecimal tienTruocGiam;
    private BigDecimal tienCoc;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTien;
    private LocalDateTime thoiGianXuat;
    private Integer idBan;
    private Integer idDatBan;
    private Integer idGiamGia;
    private Integer idKhachHang;
    private Integer idNhanVien;
    private Integer trangThaiThanhToan;
    private Integer phuongThucThanhToan;
    private BigDecimal soTienChuyenKhoan;
    private BigDecimal soTienTienMat;
    private List<DTOHoaDonChiTietRequest> chiTiet;
}
