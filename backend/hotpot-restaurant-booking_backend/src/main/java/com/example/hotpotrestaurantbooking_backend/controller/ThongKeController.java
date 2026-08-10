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

    @GetMapping("/theo-ngay")
    public List<DTOThongKeDoanhThu> theoNgay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoNgay(from, to);
    }

    // =========================================================
    // 2. DOANH THU THEO THÁNG
    // =========================================================
    @GetMapping("/theo-thang")
    public List<DTOThongKeDoanhThu> theoThang(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoThang(from, to);
    }

    // =========================================================
    // 3. DOANH THU THEO NĂM
    // =========================================================
    @GetMapping("/theo-nam")
    public List<DTOThongKeDoanhThu> theoNam(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.theoNam(from, to);
    }

    // =========================================================
    // 4. TOP NHÂN VIÊN
    // =========================================================
    @GetMapping("/top-nhan-vien")
    public List<DTOThongKeNhanVien> topNhanVien(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.topNhanVien(from, to);
    }

    // =========================================================
    // 5. TOP MÓN
    // =========================================================
    @GetMapping("/top-mon")
    public List<DTOThongKeTheoMon> topMon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.topMon(page, size, from, to);
    }

    // =========================================================
    // 6. TIỀN CỌC THEO NGÀY
    // =========================================================
    @GetMapping("/tien-coc-theo-ngay")
    public List<DTOThongKeDoanhThu> tienCocTheoNgay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.tienCocTheoNgay(from, to);
    }

    // =========================================================
    // 7. TRẠNG THÁI CỌC
    // =========================================================
    @GetMapping("/trang-thai-coc")
    public List<DTOTrangThaiCoc> trangThaiCoc(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.trangThaiCoc(from, to);
    }

    // =========================================================
    // 8. TOP KHÁCH HÀNG THÂN THIẾT
    // =========================================================
    @GetMapping("/top-khach-hang-than-thiet")
    public List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.topKhachHangThanThiet(from, to);
    }

    // =========================================================
    // 9. DASHBOARD
    // =========================================================
    @GetMapping("/dashboard")
    public DTODashboard dashboard(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.dashboard(from, to);
    }

    // =========================================================
    // 10. DOANH THU THEO KHU VỰC
    // =========================================================
    @GetMapping("/doanh-thu-theo-khu-vuc")
    public List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.doanhThuTheoKhuVuc(from, to);
    }

    // =========================================================
    // 11. HIỆU SUẤT BÀN
    // =========================================================
    @GetMapping("/hieu-suat-ban")
    public List<DTOThongKeHieuSuatBan> hieuSuatBan(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.hieuSuatBan(from, to);
    }

    // =========================================================
    // 12. TOP SẢN PHẨM BÁN CHẠY
    // =========================================================
    @GetMapping("/top-san-pham-ban-chay")
    public List<DTOThongKeSanPhamBanChay> topSanPhamBanChay(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.topSanPhamBanChay(from, to);
    }

    // =========================================================
    // 13. HIỆU QUẢ KHUYẾN MÃI
    // =========================================================
    @GetMapping("/hieu-qua-khuyen-mai")
    public List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.hieuQuaKhuyenMai(from, to);
    }

    // =========================================================
    // 14. DOANH THU THEO GIỜ
    // =========================================================
    @GetMapping("/doanh-thu-theo-gio")
    public List<DTOThongKeDoanhThu> doanhThuTheoGio(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.doanhThuTheoGio(from, to);
    }

    // =========================================================
    // 15. TỈ LỆ HỦY ĐẶT BÀN
    // =========================================================
    @GetMapping("/ty-le-huy-dat-ban")
    public List<DTOThongKeHuyDatBan> tyLeHuyDatBan(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.tyLeHuyDatBan(from, to);
    }

    // =========================================================
    // 16. DOANH THU THEO DANH MỤC
    // =========================================================
    @GetMapping("/doanh-thu-theo-danh-muc")
    public List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return service.doanhThuTheoDanhMuc(from, to);
    }
    @GetMapping("/export-excel")
    public org.springframework.http.ResponseEntity<byte[]> exportExcel(
            @RequestParam String from,
            @RequestParam String to
    ) {
        byte[] excel = service.exportExcel(from, to);

        return org.springframework.http.ResponseEntity.ok()
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=thong-ke.xlsx"
                )
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                .body(excel);
    }
}
