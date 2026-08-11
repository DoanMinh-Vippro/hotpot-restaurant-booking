package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.repository.ThongKeRepository;
import com.example.hotpotrestaurantbooking_backend.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    // ===== 2. Doanh thu theo tháng =====
    @Override
    public List<DTOThongKeDoanhThu> theoThang(String from, String to) {
        return repo.thongKeTheoThang(from, to).stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }
    @Override
    public List<DTOThongKeDoanhThu> theoNam(String from, String to) {
        return repo.thongKeTheoNam(from, to).stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 4. Top nhân viên =====
    @Override
    public List<DTOThongKeNhanVien> topNhanVien(String from, String to) {
        return repo.topNhanVien(from, to).stream()
                .map(o -> new DTOThongKeNhanVien(
                        o[0].toString(),
                        o[1] != null
                                ? Double.valueOf(o[1].toString())
                                : 0.0
                ))
                .toList();
    }

    // ===== 5. Top món =====
    @Override
    public List<DTOThongKeTheoMon> topMon(
            int page,
            int size,
            String from,
            String to
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return repo.topMon(from, to, pageable).stream()
                .map(o -> new DTOThongKeTheoMon(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Integer.valueOf(o[1].toString()) : 0
                ))
                .toList();
    }
    // ===== 6. Tiền cọc theo ngày =====
    @Override
    public List<DTOThongKeDoanhThu> tienCocTheoNgay(
            String from,
            String to
    ) {
        return repo.tienCocTheoNgay(from, to).stream()
                .map(o -> new DTOThongKeDoanhThu(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Double.valueOf(o[1].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 7. Trạng thái cọc =====
    @Override
    public List<DTOTrangThaiCoc> trangThaiCoc(
            String from,
            String to
    ) {
        return repo.trangThaiCoc(from, to).stream()
                .map(o -> new DTOTrangThaiCoc(
                        o[0] != null ? Integer.valueOf(o[0].toString()) : 0,
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L
                ))
                .toList();
    }
    @Override
    public List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet(
            String from,
            String to
    ) {
        return repo.topKhachHangThanThiet(from, to).stream()
                .map(o -> new DTOThongKeKhachHangThanThiet(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? o[1].toString() : "",
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0,
                        o[5] != null ? o[5].toString() : ""
                ))
                .toList();
    }
    // ===== 8. Dashboard tổng =====
    @Override
    public DTODashboard dashboard(
            String from,
            String to
    ) {
        Object[] o = (Object[]) repo.dashboard(from, to);

        return new DTODashboard(
                // Tổng doanh thu
                o[0] != null
                        ? Double.valueOf(o[0].toString())
                        : 0.0,

                // Doanh thu tiền mặt
                o[1] != null
                        ? Double.valueOf(o[1].toString())
                        : 0.0,

                // Doanh thu chuyển khoản
                o[2] != null
                        ? Double.valueOf(o[2].toString())
                        : 0.0,

                // Tổng hóa đơn
                o[3] != null
                        ? Long.valueOf(o[3].toString())
                        : 0L,

                // Tổng khách hàng
                o[4] != null
                        ? Long.valueOf(o[4].toString())
                        : 0L,

                // Tổng tiền cọc
                o[5] != null
                        ? Double.valueOf(o[5].toString())
                        : 0.0,

                // Số đơn đã cọc
                parseLongSafe(o[6]),

                // Số đơn chưa cọc
                parseLongSafe(o[7]),

                // Số đơn đã thanh toán / hoàn cọc
                parseLongSafe(o[8])
        );
    }

    // =========================================================
    // HELPER
    // =========================================================
    private Long parseLongSafe(Object value) {
        if (value == null) {
            return 0L;
        }

        try {
            String s = value.toString();

            if (s.contains(".")) {
                return Math.round(Double.parseDouble(s));
            }

            return Long.valueOf(s);

        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ===== 9. Doanh thu theo khu vực =====
    @Override
    public List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc(
            String from,
            String to
    ) {
        return repo.doanhThuTheoKhuVuc(from, to).stream()
                .map(o -> new DTOThongKeKhuVuc(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 10. Hiệu suất bàn =====
    @Override
    public List<DTOThongKeHieuSuatBan> hieuSuatBan(
            String from,
            String to
    ) {
        return repo.hieuSuatBan(from, to).stream()
                .map(o -> new DTOThongKeHieuSuatBan(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? o[1].toString() : "",
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }
    // ===== 11. Top sản phẩm bán chạy =====
    @Override
    public List<DTOThongKeSanPhamBanChay> topSanPhamBanChay(
            String from,
            String to
    ) {
        return repo.topSanPhamBanChay(from, to).stream()
                .map(o -> new DTOThongKeSanPhamBanChay(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? o[1].toString() : "",
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 12. Hiệu quả khuyến mãi =====
    @Override
    public List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai(
            String from,
            String to
    ) {
        return repo.hieuQuaKhuyenMai(from, to).stream()
                .map(o -> new DTOThongKeKhuyenMai(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? o[1].toString() : "",
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0,
                        o[3] != null ? Long.valueOf(o[3].toString()) : 0L,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0,
                        o[5] != null ? Double.valueOf(o[5].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 13. Doanh thu theo giờ =====
    @Override
    public List<DTOThongKeDoanhThu> doanhThuTheoGio(
            String from,
            String to
    ) {
        return repo.doanhThuTheoGio(from, to).stream()
                .map(o -> {
                    int gio = Integer.parseInt(o[0].toString());

                    String khungGio =
                            gio + "h - " + (gio + 1) + "h";

                    DTOThongKeDoanhThu dto =
                            new DTOThongKeDoanhThu();

                    dto.setThoiGian(khungGio);

                    dto.setSoHoaDon(
                            o[1] != null
                                    ? Long.valueOf(o[1].toString())
                                    : 0L
                    );

                    dto.setDoanhThu(
                            o[2] != null
                                    ? Double.valueOf(o[2].toString())
                                    : 0.0
                    );

                    return dto;
                })
                .toList();
    }



    // ===== 15. Tỉ lệ hủy đặt bàn =====
    @Override
    public List<DTOThongKeHuyDatBan> tyLeHuyDatBan(
            String fromDate,
            String toDate
    ) {
        return repo.tyLeHuyDatBan(fromDate, toDate).stream()
                .map(o -> new DTOThongKeHuyDatBan(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Double.valueOf(o[2].toString()) : 0.0
                ))
                .toList();
    }

    // ===== 16. Doanh thu theo danh mục =====
    @Override
    public List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc(
            String from,
            String to
    ) {
        return repo.doanhThuTheoDanhMuc(from, to).stream()
                .map(o -> new DTOThongKeDanhMuc(
                        o[0] != null ? o[0].toString() : "",
                        o[1] != null ? Long.valueOf(o[1].toString()) : 0L,
                        o[2] != null ? Long.valueOf(o[2].toString()) : 0L,
                        o[3] != null ? Double.valueOf(o[3].toString()) : 0.0,
                        o[4] != null ? Double.valueOf(o[4].toString()) : 0.0
                ))
                .toList();
    }
    @Override
    public byte[] exportExcel(String from, String to) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // ==========================================
            // TẠO 1 SHEET DUY NHẤT
            // ==========================================

            Sheet sheet = workbook.createSheet("Thống kê");

            int rowIndex = 0;

            // ==========================================
            // STYLE
            // ==========================================

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();

            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();

            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            headerStyle.setFillForegroundColor(
                    IndexedColors.GREY_25_PERCENT.getIndex()
            );

            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            // ==========================================
            // TIÊU ĐỀ
            // ==========================================

            Row titleRow = sheet.createRow(rowIndex++);

            Cell titleCell = titleRow.createCell(0);

            titleCell.setCellValue("BÁO CÁO THỐNG KÊ");

            titleCell.setCellStyle(titleStyle);

            // ==========================================
            // KHOẢNG THỜI GIAN
            // ==========================================

            Row dateRow = sheet.createRow(rowIndex++);

            dateRow.createCell(0)
                    .setCellValue("Từ ngày");

            dateRow.createCell(1)
                    .setCellValue(from);

            dateRow.createCell(2)
                    .setCellValue("Đến ngày");

            dateRow.createCell(3)
                    .setCellValue(to);

            rowIndex++;

            // ==========================================
            // 1. DASHBOARD
            // ==========================================

            Row dashboardTitle = sheet.createRow(rowIndex++);

            dashboardTitle.createCell(0)
                    .setCellValue("1. TỔNG QUAN");

            dashboardTitle.getCell(0)
                    .setCellStyle(titleStyle);

            DTODashboard dashboard = dashboard(from, to);

            Row dashboardHeader = sheet.createRow(rowIndex++);

            String[] dashboardColumns = {
                    "Tổng doanh thu",
                    "Tiền mặt",
                    "Chuyển khoản",
                    "Tổng hóa đơn",
                    "Khách hàng",
                    "Tiền cọc",
                    "Đã cọc",
                    "Chưa cọc"
            };

            for (int i = 0; i < dashboardColumns.length; i++) {
                dashboardHeader.createCell(i)
                        .setCellValue(dashboardColumns[i]);

                dashboardHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            Row dashboardData = sheet.createRow(rowIndex++);

            dashboardData.createCell(0)
                    .setCellValue(dashboard.getTongDoanhThu());

            dashboardData.createCell(1)
                    .setCellValue(dashboard.getDoanhThuTienMat());

            dashboardData.createCell(2)
                    .setCellValue(dashboard.getDoanhThuChuyenKhoan());

            dashboardData.createCell(3)
                    .setCellValue(dashboard.getTongHoaDon());

            dashboardData.createCell(4)
                    .setCellValue(dashboard.getTongKhachHang());

            dashboardData.createCell(5)
                    .setCellValue(dashboard.getTongTienCoc());

            dashboardData.createCell(6)
                    .setCellValue(dashboard.getSoDonDaCoc());

            dashboardData.createCell(7)
                    .setCellValue(dashboard.getSoDonChuaCoc());

            rowIndex += 2;

            // ==========================================
            // 2. DOANH THU THEO NGÀY
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "2. DOANH THU THEO NGÀY",
                    titleStyle
            );

            rowIndex = writeDoanhThu(
                    sheet,
                    rowIndex,
                    theoNgay(from, to),
                    headerStyle
            );

            rowIndex += 2;

            // ==========================================
            // 3. DOANH THU THEO THÁNG
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "3. DOANH THU THEO THÁNG",
                    titleStyle
            );

            rowIndex = writeDoanhThu(
                    sheet,
                    rowIndex,
                    theoThang(from, to),
                    headerStyle
            );

            rowIndex += 2;

            // ==========================================
            // 4. DOANH THU THEO NĂM
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "4. DOANH THU THEO NĂM",
                    titleStyle
            );

            rowIndex = writeDoanhThu(
                    sheet,
                    rowIndex,
                    theoNam(from, to),
                    headerStyle
            );

            rowIndex += 2;

            // ==========================================
            // 5. TOP NHÂN VIÊN
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "5. TOP NHÂN VIÊN",
                    titleStyle
            );

            Row nvHeader = sheet.createRow(rowIndex++);

            nvHeader.createCell(0).setCellValue("Nhân viên");
            nvHeader.createCell(1).setCellValue("Doanh thu");

            nvHeader.getCell(0).setCellStyle(headerStyle);
            nvHeader.getCell(1).setCellStyle(headerStyle);

            for (DTOThongKeNhanVien nv : topNhanVien(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(nv.getTenNhanVien());

                row.createCell(1)
                        .setCellValue(nv.getTongDoanhThu());
            }

            rowIndex += 2;

            // ==========================================
            // 6. TOP MÓN
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "6. TOP MÓN BÁN CHẠY",
                    titleStyle
            );

            Row monHeader = sheet.createRow(rowIndex++);

            monHeader.createCell(0).setCellValue("Món");
            monHeader.createCell(1).setCellValue("Số lượng bán");

            monHeader.getCell(0).setCellStyle(headerStyle);
            monHeader.getCell(1).setCellStyle(headerStyle);

            for (DTOThongKeTheoMon mon :
                    topMon(0, 1000, from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(mon.getTenMon());

                row.createCell(1)
                        .setCellValue(mon.getSoLuongBan());
            }

            rowIndex += 2;

            // ==========================================
            // 7. TRẠNG THÁI CỌC
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "7. TRẠNG THÁI CỌC",
                    titleStyle
            );

            Row cocHeader = sheet.createRow(rowIndex++);

            cocHeader.createCell(0)
                    .setCellValue("Trạng thái");

            cocHeader.createCell(1)
                    .setCellValue("Số lượng");

            cocHeader.getCell(0).setCellStyle(headerStyle);
            cocHeader.getCell(1).setCellStyle(headerStyle);

            for (DTOTrangThaiCoc coc : trangThaiCoc(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                String trangThai = coc.getTrangThai() == 1
                        ? "Đã cọc"
                        : "Chưa cọc";

                row.createCell(0)
                        .setCellValue(trangThai);

                row.createCell(1)
                        .setCellValue(coc.getSoLuong());
            }

            rowIndex += 2;

            // ==========================================
            // 8. KHU VỰC
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "8. DOANH THU THEO KHU VỰC",
                    titleStyle
            );

            Row kvHeader = sheet.createRow(rowIndex++);

            kvHeader.createCell(0).setCellValue("Khu vực");
            kvHeader.createCell(1).setCellValue("Số hóa đơn");
            kvHeader.createCell(2).setCellValue("Doanh thu");
            kvHeader.createCell(3).setCellValue("Trung bình");

            for (int i = 0; i < 4; i++) {
                kvHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhuVuc kv : doanhThuTheoKhuVuc(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(kv.getKhuVuc());

                row.createCell(1)
                        .setCellValue(kv.getSoHoaDon());

                row.createCell(2)
                        .setCellValue(kv.getDoanhThu());

                row.createCell(3)
                        .setCellValue(kv.getTrungBinhHoaDon());
            }

            rowIndex += 2;

            // ==========================================
            // 9. TOP KHÁCH HÀNG
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "9. TOP KHÁCH HÀNG",
                    titleStyle
            );

            Row khHeader = sheet.createRow(rowIndex++);

            khHeader.createCell(0).setCellValue("Khách hàng");
            khHeader.createCell(1).setCellValue("Số điện thoại");
            khHeader.createCell(2).setCellValue("Số hóa đơn");
            khHeader.createCell(3).setCellValue("Tổng chi tiêu");

            for (int i = 0; i < 4; i++) {
                khHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhachHangThanThiet kh :
                    topKhachHangThanThiet(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(kh.getTenKhachHang());

                row.createCell(1)
                        .setCellValue(kh.getSoDienThoai());

                row.createCell(2)
                        .setCellValue(kh.getSoLanDen());

                row.createCell(3)
                        .setCellValue(kh.getTongChiTieu());
            }

            rowIndex += 2;

            // ==========================================
            // 10. KHUYẾN MÃI
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "10. HIỆU QUẢ KHUYẾN MÃI",
                    titleStyle
            );

            Row kmHeader = sheet.createRow(rowIndex++);

            kmHeader.createCell(0).setCellValue("Mã giảm giá");
            kmHeader.createCell(1).setCellValue("Số lần sử dụng");
            kmHeader.createCell(2).setCellValue("Tiền đã giảm");

            for (int i = 0; i < 3; i++) {
                kmHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhuyenMai km :
                    hieuQuaKhuyenMai(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(km.getMaGiamGia());

                row.createCell(1)
                        .setCellValue(km.getSoLanSuDung());

                row.createCell(2)
                        .setCellValue(km.getTongTienDaGiam());
            }

            rowIndex += 2;

            // ==========================================
            // 11. DOANH THU THEO GIỜ
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "11. DOANH THU THEO GIỜ",
                    titleStyle
            );

            Row gioHeader = sheet.createRow(rowIndex++);

            gioHeader.createCell(0).setCellValue("Khung giờ");
            gioHeader.createCell(1).setCellValue("Số hóa đơn");
            gioHeader.createCell(2).setCellValue("Doanh thu");

            for (int i = 0; i < 3; i++) {
                gioHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeDoanhThu gio :
                    doanhThuTheoGio(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(gio.getThoiGian());

                row.createCell(1)
                        .setCellValue(gio.getSoHoaDon());

                row.createCell(2)
                        .setCellValue(gio.getDoanhThu());
            }

            rowIndex += 2;

            // ==========================================
            // 12. DOANH THU THEO DANH MỤC
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "12. DOANH THU THEO DANH MỤC",
                    titleStyle
            );

            Row dmHeader = sheet.createRow(rowIndex++);

            dmHeader.createCell(0).setCellValue("Danh mục");
            dmHeader.createCell(1).setCellValue("Số hóa đơn");
            dmHeader.createCell(2).setCellValue("Số lượng bán");
            dmHeader.createCell(3).setCellValue("Tổng thu");

            for (int i = 0; i < 4; i++) {
                dmHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeDanhMuc dm :
                    doanhThuTheoDanhMuc(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(dm.getDanhMuc());

                row.createCell(1)
                        .setCellValue(dm.getSoHoaDon());

                row.createCell(2)
                        .setCellValue(dm.getSoLuongBan());

                row.createCell(3)
                        .setCellValue(dm.getTongThu());
            }

            rowIndex += 2;

            // ==========================================
            // 13. HIỆU SUẤT BÀN
            // ==========================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "13. HIỆU SUẤT BÀN",
                    titleStyle
            );

            Row banHeader = sheet.createRow(rowIndex++);

            banHeader.createCell(0).setCellValue("Bàn");
            banHeader.createCell(1).setCellValue("Khu vực");
            banHeader.createCell(2).setCellValue("Số lần phục vụ");
            banHeader.createCell(3).setCellValue("Doanh thu");

            for (int i = 0; i < 4; i++) {
                banHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeHieuSuatBan ban :
                    hieuSuatBan(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(ban.getTenBan());

                row.createCell(1)
                        .setCellValue(ban.getKhuVuc());

                row.createCell(2)
                        .setCellValue(ban.getSoLanPhucVu());

                row.createCell(3)
                        .setCellValue(ban.getTongDoanhThu());
            }

            // ==========================================
            // AUTO SIZE
            // ==========================================

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            // ==========================================
            // GHI FILE
            // ==========================================

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể tạo file Excel",
                    e
            );
        }
    }
    private int writeTitle(
            Sheet sheet,
            int rowIndex,
            String title,
            CellStyle style
    ) {
        Row row = sheet.createRow(rowIndex++);

        Cell cell = row.createCell(0);

        cell.setCellValue(title);
        cell.setCellStyle(style);

        return rowIndex;
    }
    private int writeDoanhThu(
            Sheet sheet,
            int rowIndex,
            List<DTOThongKeDoanhThu> data,
            CellStyle headerStyle
    ) {

        // Header
        Row header = sheet.createRow(rowIndex++);

        header.createCell(0)
                .setCellValue("Thời gian");

        header.createCell(1)
                .setCellValue("Số hóa đơn");

        header.createCell(2)
                .setCellValue("Tổng tiền");

        header.createCell(3)
                .setCellValue("Giảm giá");

        header.createCell(4)
                .setCellValue("Doanh thu");

        // Style header
        for (int i = 0; i < 5; i++) {
            header.getCell(i)
                    .setCellStyle(headerStyle);
        }

        // Data
        for (DTOThongKeDoanhThu item : data) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0)
                    .setCellValue(
                            item.getThoiGian() != null
                                    ? item.getThoiGian()
                                    : ""
                    );

            row.createCell(1)
                    .setCellValue(
                            item.getSoHoaDon() != null
                                    ? item.getSoHoaDon()
                                    : 0
                    );

            row.createCell(2)
                    .setCellValue(
                            item.getTongTien() != null
                                    ? item.getTongTien()
                                    : 0.0
                    );

            row.createCell(3)
                    .setCellValue(
                            item.getGiamGia() != null
                                    ? item.getGiamGia()
                                    : 0.0
                    );

            row.createCell(4)
                    .setCellValue(
                            item.getDoanhThu() != null
                                    ? item.getDoanhThu()
                                    : 0.0
                    );
        }

        return rowIndex;
    }
}
