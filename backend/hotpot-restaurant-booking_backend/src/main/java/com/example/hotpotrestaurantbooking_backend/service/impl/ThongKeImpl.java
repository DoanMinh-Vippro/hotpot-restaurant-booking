package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.repository.ThongKeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongKeImpl implements ThongKeService {

    @Autowired
    private ThongKeRepository repo;

    // ===== 1. Doanh thu theo ngày =====
    @Override
    public List<DTOThongKeDoanhThu> theoNgay(String from, String to) {
        return repo.doanhThuTheoNgay(from, to).stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 3. Doanh thu theo năm =====
    @Override
    public List<DTOThongKeDoanhThu> theoNam() {
        return repo.thongKeTheoNam().stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        o[1] != null ? Double.valueOf(o[1].toString()) : 0.0
                ))
                .toList();
    }
    // ===== 2. Doanh thu theo tháng =====
    @Override
    public List<DTOThongKeDoanhThu> theoThang() {
        return repo.thongKeTheoThang().stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        o[1] != null ? Double.valueOf(o[1].toString()) : 0.0
                ))
                .toList();
    }
    // ===== 4. Top nhân viên =====
    @Override
    public List<DTOThongKeNhanVien> topNhanVien() {
        return repo.topNhanVien().stream()
                .map(o -> new DTOThongKeNhanVien(
                        o[0].toString(),
                        o[1] != null ? Double.valueOf(o[1].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 5. Top món =====
    @Override
    public List<DTOThongKeTheoMon> topMon(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.topMon(pageable).stream()
                .map(o -> new DTOThongKeTheoMon(
                        o[0].toString(),
                        Integer.valueOf(o[1].toString())
                ))
                .toList();
    }

    // ===== 6. Tiền cọc theo ngày =====
    @Override
    public List<DTOThongKeDoanhThu> tienCocTheoNgay() {
        return repo.tienCocTheoNgay().stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0].toString(),
                        o[1] != null ? Double.valueOf(o[1].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 7. Trạng thái cọc =====
    @Override
    public List<DTOTrangThaiCoc> trangThaiCoc() {
        return repo.trangThaiCoc().stream()
                .map(o -> new DTOTrangThaiCoc(
                        Integer.valueOf(o[0].toString()),
                        Long.valueOf(o[1].toString())
                ))
                .toList();
    }
    @Override
    public List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet() {
        return repo.topKhachHangThanThiet().stream()
                .map(o -> new DTOThongKeKhachHangThanThiet(
                        o[0].toString(),                                     // tên khách hàng
                        o[1].toString(),                                     // số điện thoại
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,   // số lần đến
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0, // tổng chi tiêu
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0, // trung bình hóa đơn
                        o[5] != null ? o[5].toString() : ""                  // lần cuối đến
                ))
                .toList();
    }
    // ===== 8. Dashboard tổng =====
    @Override
    public DTODashboard dashboard() {
        Object[] o = (Object[]) repo.dashboard();
        return new DTODashboard(
                o[0] != null ? Double.valueOf(o[0].toString()) : 0.0,   // tongDoanhThu
                o[1] != null ? Long.valueOf(o[1].toString()) : 0L,      // tongHoaDon
                o[2] != null ? Long.valueOf(o[2].toString()) : 0L,      // tongKhachHang
                o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,   // tongTienCoc
                parseLongSafe(o[4]),   // soDonDaCoc    ← SỬA
                parseLongSafe(o[5]),   // soDonChuaCoc  ← SỬA
                parseLongSafe(o[6])    // soDonHoanCoc  ← SỬA
        );
    }

    // Thêm helper method này vào cuối class
    private Long parseLongSafe(Object value) {
        if (value == null) return 0L;
        try {
            String s = value.toString();
            if (s.contains(".")) {
                return Long.valueOf(Math.round(Double.parseDouble(s)));
            }
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ===== 9. Doanh thu theo khu vực =====
    @Override
    public List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc() {
        return repo.doanhThuTheoKhuVuc().stream()
                .map(o -> new DTOThongKeKhuVuc(
                        o[0].toString(),
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 10. Hiệu suất bàn =====
    @Override
    public List<DTOThongKeHieuSuatBan> hieuSuatBan() {
        return repo.hieuSuatBan().stream()
                .map(o -> new DTOThongKeHieuSuatBan(
                        o[0].toString(),
                        o[1].toString(),
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 11. Top sản phẩm bán chạy =====
    @Override
    public List<DTOThongKeSanPhamBanChay> topSanPhamBanChay() {
        return repo.topSanPhamBanChay().stream()
                .map(o -> new DTOThongKeSanPhamBanChay(
                        o[0].toString(),
                        o[1].toString(),
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 12. Hiệu quả khuyến mãi =====
    @Override
    public List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai() {
        return repo.hieuQuaKhuyenMai().stream()
                .map(o -> new DTOThongKeKhuyenMai(
                        o[0].toString(),
                        o[1].toString(),
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Long.valueOf(o[3].toString()) : 0L,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0,
                        o[5] != null ? Double.valueOf(o[5].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 13. Doanh thu theo giờ =====
    @Override
    public List<DTOThongKeDoanhThu> doanhThuTheoGio() {
        return repo.doanhThuTheoGio().stream()
                .map(o -> {
                    int gio = Integer.parseInt(o[0].toString());
                    String khungGio = gio + "h - " + (gio + 1) + "h";

                    DTOThongKeDoanhThu dto = new DTOThongKeDoanhThu();
                    dto.setThoiGian(khungGio);
                    dto.setSoHoaDon(o[1] != null ? Long.valueOf(o[1].toString()) : 0L);
                    dto.setDoanhThu(o[2] != null ? Double.valueOf(o[2].toString()) : 0.0);
                    return dto;
                })
                .toList();
    }



    // ===== 15. Tỉ lệ hủy đặt bàn =====
    @Override
    public List<DTOThongKeHuyDatBan> tyLeHuyDatBan(String fromDate, String toDate) {
        return repo.tyLeHuyDatBan(fromDate, toDate).stream()
                .map(o -> new DTOThongKeHuyDatBan(
                        o[0].toString(),
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 16. Doanh thu theo danh mục =====
    @Override
    public List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc() {
        return repo.doanhThuTheoDanhMuc().stream()
                .map(o -> new DTOThongKeDanhMuc(
                        o[0].toString(),
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }
}
