package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/thong-ke")
public class ThongKeController {

    @Autowired
    private ThongKeService service;

    // ==========================================
    // CÁC ENDPOINT CŨ (GIỮ NGUYÊN)
    // ==========================================

    // 1. Doanh thu theo ngày
    @GetMapping("/theo-ngay")
    public List<DTOThongKeDoanhThu> theoNgay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoNgay(from, to);
    }

    // 2. Doanh thu theo tháng
    @GetMapping("/theo-thang")
    public List<DTOThongKeDoanhThu> theoThang() {
        return service.theoThang();
    }

    // 3. Doanh thu theo năm
    @GetMapping("/theo-nam")
    public List<DTOThongKeDoanhThu> theoNam() {
        return service.theoNam();
    }

    // 4. Top nhân viên
    @GetMapping("/top-nhan-vien")
    public List<DTOThongKeNhanVien> topNhanVien() {
        return service.topNhanVien();
    }

    // 5. Top món
    @GetMapping("/top-mon")
    public List<DTOThongKeTheoMon> topMon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return service.topMon(page, size);
    }

    // 6. Tiền cọc theo ngày
    @GetMapping("/tien-coc-theo-ngay")
    public List<DTOThongKeDoanhThu> tienCocTheoNgay() {
        return service.tienCocTheoNgay();
    }

    // 7. Trạng thái cọc
    @GetMapping("/trang-thai-coc")
    public List<DTOTrangThaiCoc> trangThaiCoc() {
        return service.trangThaiCoc();
    }
    @GetMapping("/top-khach-hang-than-thiet")
    public List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet() {
        return service.topKhachHangThanThiet();
    }
    // 8. Dashboard tổng quan
    @GetMapping("/dashboard")
    public DTODashboard dashboard() {
        return service.dashboard();
    }

    // ==========================================
    // CÁC ENDPOINT MỚI (BỔ SUNG)
    // ==========================================

    // 9. Doanh thu theo khu vực (Tầng/VIP)
    @GetMapping("/doanh-thu-theo-khu-vuc")
    public List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc() {
        return service.doanhThuTheoKhuVuc();
    }

    // 10. Hiệu suất từng bàn
    @GetMapping("/hieu-suat-ban")
    public List<DTOThongKeHieuSuatBan> hieuSuatBan() {
        return service.hieuSuatBan();
    }

    // 11. Top sản phẩm bán chạy (Món + Combo)
    @GetMapping("/top-san-pham-ban-chay")
    public List<DTOThongKeSanPhamBanChay> topSanPhamBanChay() {
        return service.topSanPhamBanChay();
    }

    // 12. Hiệu quả khuyến mãi
    @GetMapping("/hieu-qua-khuyen-mai")
    public List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai() {
        return service.hieuQuaKhuyenMai();
    }

    // 13. Doanh thu theo khung giờ (phân tích giờ cao điểm)
    @GetMapping("/doanh-thu-theo-gio")
    public List<DTOThongKeDoanhThu> doanhThuTheoGio() {
        return service.doanhThuTheoGio();
    }



    // 15. Tỉ lệ hủy đặt bàn
    @GetMapping("/ty-le-huy-dat-ban")
    public List<DTOThongKeHuyDatBan> tyLeHuyDatBan(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.tyLeHuyDatBan(from, to);
    }

    // 16. Doanh thu theo danh mục món
    @GetMapping("/doanh-thu-theo-danh-muc")
    public List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc() {
        return service.doanhThuTheoDanhMuc();
    }
}
