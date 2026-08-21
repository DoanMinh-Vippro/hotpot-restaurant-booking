package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.repository.ThongKeRepository;
import com.example.hotpotrestaurantbooking_backend.service.ThongKeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ThongKeImpl implements ThongKeService {

    @Autowired
    private ThongKeRepository repo;

    // =========================================================
    // 1. DOANH THU THEO NGÀY
    // =========================================================

    @Override
    public List<DTOThongKeDoanhThu> theoNgay(String from, String to) {
        return repo.doanhThuTheoNgay(from, to)
                .stream()
                .map(o -> new DTOThongKeDoanhThu(
                        stringValue(o, 0),
                        longValue(o, 1),
                        doubleValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4)
                ))
                .toList();
    }

    // =========================================================
    // 2. DOANH THU THEO THÁNG
    // =========================================================

    @Override
    public List<DTOThongKeDoanhThu> theoThang(String from, String to) {
        return repo.thongKeTheoThang(from, to)
                .stream()
                .map(o -> new DTOThongKeDoanhThu(
                        stringValue(o, 0),
                        longValue(o, 1),
                        doubleValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4)
                ))
                .toList();
    }

    // =========================================================
    // 3. DOANH THU THEO NĂM
    // =========================================================

    @Override
    public List<DTOThongKeDoanhThu> theoNam(String from, String to) {
        return repo.thongKeTheoNam(from, to)
                .stream()
                .map(o -> new DTOThongKeDoanhThu(
                        stringValue(o, 0),
                        longValue(o, 1),
                        doubleValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4)
                ))
                .toList();
    }

    // =========================================================
    // 4. TOP NHÂN VIÊN
    // =========================================================

    @Override
    public List<DTOThongKeNhanVien> topNhanVien(
            String from,
            String to
    ) {
        return repo.topNhanVien(from, to)
                .stream()
                .map(o -> new DTOThongKeNhanVien(
                        stringValue(o, 0),
                        doubleValue(o, 1)
                ))
                .toList();
    }

    // =========================================================
    // 5. TOP MÓN
    // =========================================================

    @Override
    public List<DTOThongKeTheoMon> topMon(
            int page,
            int size,
            String from,
            String to
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return repo.topMon(from, to, pageable)
                .stream()
                .map(o -> new DTOThongKeTheoMon(
                        stringValue(o, 0),
                        intValue(o, 1)
                ))
                .toList();
    }

    // =========================================================
    // 6. TIỀN CỌC THEO NGÀY
    // =========================================================

    @Override
    public List<DTOThongKeDoanhThu> tienCocTheoNgay(
            String from,
            String to
    ) {

        return repo.tienCocTheoNgay(from, to)
                .stream()
                .map(o -> {

                    DTOThongKeDoanhThu dto =
                            new DTOThongKeDoanhThu();

                    dto.setThoiGian(stringValue(o, 0));

                    // Nếu DTO đang dùng doanhThu để chứa tiền cọc
                    dto.setDoanhThu(doubleValue(o, 1));

                    return dto;
                })
                .toList();
    }

    // =========================================================
    // 7. TRẠNG THÁI CỌC
    // =========================================================

    @Override
    public List<DTOTrangThaiCoc> trangThaiCoc(
            String from,
            String to
    ) {

        return repo.trangThaiCoc(from, to)
                .stream()
                .map(o -> new DTOTrangThaiCoc(
                        intValue(o, 0),
                        longValue(o, 1)
                ))
                .toList();
    }

    // =========================================================
    // TOP KHÁCH HÀNG THÂN THIẾT
    // =========================================================

    @Override
    public List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet(
            String from,
            String to
    ) {

        return repo.topKhachHangThanThiet(from, to)
                .stream()
                .map(o -> new DTOThongKeKhachHangThanThiet(
                        stringValue(o, 0),
                        stringValue(o, 1),
                        longValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4),
                        stringValue(o, 5)
                ))
                .toList();
    }

    // =========================================================
    // 8. DASHBOARD
    // =========================================================

    @Override
    public DTODashboard dashboard(String from, String to) {

        Object result = repo.dashboard(from, to);

        Object[] o;

        if (result instanceof Object[]) {
            o = (Object[]) result;
        } else {
            return new DTODashboard(
                    0.0, // tongDoanhThu
                    0.0, // doanhThuTienMat
                    0.0, // doanhThuChuyenKhoan
                    0L,  // tongHoaDon
                    0L,  // tongKhachHang
                    0L,  // soDonDaCoc
                    0L,  // soDonChuaCoc
                    0.0, // tienCocTienMat
                    0.0, // tienCocChuyenKhoan
                    0L   // soDonDaThanhToan
            );
        }

        return new DTODashboard(

                // 0 - Tổng doanh thu
                doubleValue(o, 0),

                // 1 - Doanh thu tiền mặt
                doubleValue(o, 1),

                // 2 - Doanh thu chuyển khoản
                doubleValue(o, 2),

                // 3 - Tổng hóa đơn
                longValue(o, 3),

                // 4 - Tổng khách hàng
                longValue(o, 4),

                // 5 - Số đơn đã cọc
                longValue(o, 5),

                // 6 - Số đơn chưa cọc
                longValue(o, 6),

                // 7 - Tiền cọc tiền mặt
                doubleValue(o, 7),

                // 8 - Tiền cọc chuyển khoản
                doubleValue(o, 8),

                // 9 - Số đơn đã thanh toán
                longValue(o, 9)
        );
    }

    // =========================================================
    // 9. DOANH THU THEO KHU VỰC
    // =========================================================

    @Override
    public List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc(
            String from,
            String to
    ) {

        return repo.doanhThuTheoKhuVuc(from, to)
                .stream()
                .map(o -> new DTOThongKeKhuVuc(
                        stringValue(o, 0),
                        longValue(o, 1),
                        doubleValue(o, 2),
                        doubleValue(o, 3)
                ))
                .toList();
    }

    // =========================================================
    // 10. HIỆU SUẤT BÀN
    // =========================================================

    @Override
    public List<DTOThongKeHieuSuatBan> hieuSuatBan(
            String from,
            String to
    ) {

        return repo.hieuSuatBan(from, to)
                .stream()
                .map(o -> new DTOThongKeHieuSuatBan(
                        stringValue(o, 0),
                        stringValue(o, 1),
                        longValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4)
                ))
                .toList();
    }

    // =========================================================
    // 11. TOP SẢN PHẨM BÁN CHẠY
    // =========================================================

    @Override
    public List<DTOThongKeSanPhamBanChay> topSanPhamBanChay(
            String from,
            String to
    ) {

        return repo.topSanPhamBanChay(from, to)
                .stream()
                .map(o -> new DTOThongKeSanPhamBanChay(
                        stringValue(o, 0),
                        stringValue(o, 1),
                        longValue(o, 2),
                        doubleValue(o, 3)
                ))
                .toList();
    }

    // =========================================================
    // 12. HIỆU QUẢ KHUYẾN MÃI
    // =========================================================

    @Override
    public List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai(
            String from,
            String to
    ) {

        return repo.hieuQuaKhuyenMai(from, to)
                .stream()
                .map(o -> new DTOThongKeKhuyenMai(
                        stringValue(o, 0),
                        stringValue(o, 1),
                        doubleValue(o, 2),
                        longValue(o, 3),
                        doubleValue(o, 4),
                        doubleValue(o, 5)
                ))
                .toList();
    }

    // =========================================================
    // 13. DOANH THU THEO GIỜ
    // =========================================================

    @Override
    public List<DTOThongKeDoanhThu> doanhThuTheoGio(
            String from,
            String to
    ) {

        return repo.doanhThuTheoGio(from, to)
                .stream()
                .map(o -> {

                    int gio = intValue(o, 0);

                    String khungGio =
                            gio + "h - " + (gio + 1) + "h";

                    DTOThongKeDoanhThu dto =
                            new DTOThongKeDoanhThu();

                    dto.setThoiGian(khungGio);

                    dto.setSoHoaDon(
                            longValue(o, 1)
                    );

                    dto.setDoanhThu(
                            doubleValue(o, 2)
                    );

                    return dto;
                })
                .toList();
    }

    // =========================================================
    // 14. TỶ LỆ HỦY ĐẶT BÀN
    // =========================================================

    @Override
    public List<DTOThongKeHuyDatBan> tyLeHuyDatBan(
            String fromDate,
            String toDate
    ) {

        return repo.tyLeHuyDatBan(fromDate, toDate)
                .stream()
                .map(o -> new DTOThongKeHuyDatBan(
                        stringValue(o, 0),
                        longValue(o, 1),
                        doubleValue(o, 2)
                ))
                .toList();
    }

    // =========================================================
    // 15. DOANH THU THEO DANH MỤC
    // =========================================================

    @Override
    public List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc(
            String from,
            String to
    ) {

        return repo.doanhThuTheoDanhMuc(from, to)
                .stream()
                .map(o -> new DTOThongKeDanhMuc(
                        stringValue(o, 0),
                        longValue(o, 1),
                        longValue(o, 2),
                        doubleValue(o, 3),
                        doubleValue(o, 4)
                ))
                .toList();
    }

    // =========================================================
    // EXPORT EXCEL
    // =========================================================

    @Override
    public byte[] exportExcel(
            String from,
            String to
    ) {

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Thống kê");

            int rowIndex = 0;

            // =====================================================
            // STYLE
            // =====================================================

            CellStyle titleStyle =
                    workbook.createCellStyle();

            Font titleFont =
                    workbook.createFont();

            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            titleStyle.setFont(titleFont);

            CellStyle headerStyle =
                    workbook.createCellStyle();

            Font headerFont =
                    workbook.createFont();

            headerFont.setBold(true);

            headerStyle.setFont(headerFont);

            headerStyle.setFillForegroundColor(
                    IndexedColors.GREY_25_PERCENT.getIndex()
            );

            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            // =====================================================
            // TIÊU ĐỀ
            // =====================================================

            Row titleRow =
                    sheet.createRow(rowIndex++);

            Cell titleCell =
                    titleRow.createCell(0);

            titleCell.setCellValue(
                    "BÁO CÁO THỐNG KÊ"
            );

            titleCell.setCellStyle(titleStyle);

            // =====================================================
            // THỜI GIAN
            // =====================================================

            Row dateRow =
                    sheet.createRow(rowIndex++);

            dateRow.createCell(0)
                    .setCellValue("Từ ngày");

            dateRow.createCell(1)
                    .setCellValue(from);

            dateRow.createCell(2)
                    .setCellValue("Đến ngày");

            dateRow.createCell(3)
                    .setCellValue(to);

            rowIndex += 2;

            // =====================================================
            // 1. DASHBOARD
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "1. TỔNG QUAN",
                    titleStyle
            );

            DTODashboard dashboard =
                    dashboard(from, to);

            Row dashboardHeader =
                    sheet.createRow(rowIndex++);

            String[] dashboardColumns = {

                    "Tổng doanh thu",
                    "Tiền mặt",
                    "Chuyển khoản",
                    "Tổng hóa đơn",
                    "Khách hàng",
                    "Tiền cọc",
                    "Đã cọc",
                    "Chưa cọc",
                    "Đã thanh toán / hoàn cọc"
            };

            for (int i = 0;
                 i < dashboardColumns.length;
                 i++) {

                dashboardHeader
                        .createCell(i)
                        .setCellValue(
                                dashboardColumns[i]
                        );

                dashboardHeader
                        .getCell(i)
                        .setCellStyle(headerStyle);
            }

            Row dashboardData =
                    sheet.createRow(rowIndex++);

            dashboardData.createCell(0)
                    .setCellValue(
                            dashboard.getTongDoanhThu()
                    );

            dashboardData.createCell(1)
                    .setCellValue(
                            dashboard.getDoanhThuTienMat()
                    );

            dashboardData.createCell(2)
                    .setCellValue(
                            dashboard.getDoanhThuChuyenKhoan()
                    );

            dashboardData.createCell(3)
                    .setCellValue(
                            dashboard.getTongHoaDon()
                    );

            dashboardData.createCell(4)
                    .setCellValue(
                            dashboard.getTongKhachHang()
                    );

            double tongTienCoc =
                    (dashboard.getTienCocTienMat() != null
                            ? dashboard.getTienCocTienMat()
                            : 0.0)
                            +
                            (dashboard.getTienCocChuyenKhoan() != null
                                    ? dashboard.getTienCocChuyenKhoan()
                                    : 0.0);

            dashboardData.createCell(5)
                    .setCellValue(tongTienCoc);
            dashboardData.createCell(6)
                    .setCellValue(
                            dashboard.getSoDonDaCoc()
                    );

            dashboardData.createCell(7)
                    .setCellValue(
                            dashboard.getSoDonChuaCoc()
                    );

            dashboardData.createCell(8)
                    .setCellValue(
                            dashboard.getSoDonDaThanhToan()
                    );

            rowIndex += 2;

            // =====================================================
            // 2. DOANH THU NGÀY
            // =====================================================

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

            // =====================================================
            // 3. DOANH THU THÁNG
            // =====================================================

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

            // =====================================================
            // 4. DOANH THU NĂM
            // =====================================================

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

            // =====================================================
            // 5. TOP NHÂN VIÊN
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "5. TOP NHÂN VIÊN",
                    titleStyle
            );

            Row nvHeader =
                    sheet.createRow(rowIndex++);

            nvHeader.createCell(0)
                    .setCellValue("Nhân viên");

            nvHeader.createCell(1)
                    .setCellValue("Doanh thu");

            nvHeader.getCell(0)
                    .setCellStyle(headerStyle);

            nvHeader.getCell(1)
                    .setCellStyle(headerStyle);

            for (DTOThongKeNhanVien nv :
                    topNhanVien(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                nv.getTenNhanVien()
                        );

                row.createCell(1)
                        .setCellValue(
                                nv.getTongDoanhThu()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 6. TOP MÓN
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "6. TOP MÓN BÁN CHẠY",
                    titleStyle
            );

            Row monHeader =
                    sheet.createRow(rowIndex++);

            monHeader.createCell(0)
                    .setCellValue("Món");

            monHeader.createCell(1)
                    .setCellValue("Số lượng bán");

            monHeader.getCell(0)
                    .setCellStyle(headerStyle);

            monHeader.getCell(1)
                    .setCellStyle(headerStyle);

            for (DTOThongKeTheoMon mon :
                    topMon(0, 1000, from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                mon.getTenMon()
                        );

                row.createCell(1)
                        .setCellValue(
                                mon.getSoLuongBan()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 7. TRẠNG THÁI CỌC
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "7. TRẠNG THÁI CỌC",
                    titleStyle
            );

            Row cocHeader =
                    sheet.createRow(rowIndex++);

            cocHeader.createCell(0)
                    .setCellValue("Trạng thái");

            cocHeader.createCell(1)
                    .setCellValue("Số lượng");

            cocHeader.getCell(0)
                    .setCellStyle(headerStyle);

            cocHeader.getCell(1)
                    .setCellStyle(headerStyle);

            for (DTOTrangThaiCoc coc :
                    trangThaiCoc(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                String trangThai =
                        coc.getTrangThai() == 1
                                ? "Đã cọc"
                                : "Chưa cọc";

                row.createCell(0)
                        .setCellValue(trangThai);

                row.createCell(1)
                        .setCellValue(
                                coc.getSoLuong()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 8. KHU VỰC
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "8. DOANH THU THEO KHU VỰC",
                    titleStyle
            );

            Row kvHeader =
                    sheet.createRow(rowIndex++);

            String[] kvColumns = {
                    "Khu vực",
                    "Số hóa đơn",
                    "Doanh thu",
                    "Trung bình"
            };

            for (int i = 0; i < kvColumns.length; i++) {

                kvHeader.createCell(i)
                        .setCellValue(kvColumns[i]);

                kvHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhuVuc kv :
                    doanhThuTheoKhuVuc(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                kv.getKhuVuc()
                        );

                row.createCell(1)
                        .setCellValue(
                                kv.getSoHoaDon()
                        );

                row.createCell(2)
                        .setCellValue(
                                kv.getDoanhThu()
                        );

                row.createCell(3)
                        .setCellValue(
                                kv.getTrungBinhHoaDon()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 9. TOP KHÁCH HÀNG
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "9. TOP KHÁCH HÀNG",
                    titleStyle
            );

            Row khHeader =
                    sheet.createRow(rowIndex++);

            String[] khColumns = {
                    "Khách hàng",
                    "Số điện thoại",
                    "Số hóa đơn",
                    "Tổng chi tiêu"
            };

            for (int i = 0; i < khColumns.length; i++) {

                khHeader.createCell(i)
                        .setCellValue(khColumns[i]);

                khHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhachHangThanThiet kh :
                    topKhachHangThanThiet(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                kh.getTenKhachHang()
                        );

                row.createCell(1)
                        .setCellValue(
                                kh.getSoDienThoai()
                        );

                row.createCell(2)
                        .setCellValue(
                                kh.getSoLanDen()
                        );

                row.createCell(3)
                        .setCellValue(
                                kh.getTongChiTieu()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 10. KHUYẾN MÃI
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "10. HIỆU QUẢ KHUYẾN MÃI",
                    titleStyle
            );

            Row kmHeader =
                    sheet.createRow(rowIndex++);

            String[] kmColumns = {
                    "Mã giảm giá",
                    "Số lần sử dụng",
                    "Tiền đã giảm"
            };

            for (int i = 0; i < kmColumns.length; i++) {

                kmHeader.createCell(i)
                        .setCellValue(kmColumns[i]);

                kmHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeKhuyenMai km :
                    hieuQuaKhuyenMai(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                km.getMaGiamGia()
                        );

                row.createCell(1)
                        .setCellValue(
                                km.getSoLanSuDung()
                        );

                row.createCell(2)
                        .setCellValue(
                                km.getTongTienDaGiam()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 11. DOANH THU THEO GIỜ
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "11. DOANH THU THEO GIỜ",
                    titleStyle
            );

            Row gioHeader =
                    sheet.createRow(rowIndex++);

            String[] gioColumns = {
                    "Khung giờ",
                    "Số hóa đơn",
                    "Doanh thu"
            };

            for (int i = 0; i < gioColumns.length; i++) {

                gioHeader.createCell(i)
                        .setCellValue(gioColumns[i]);

                gioHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeDoanhThu gio :
                    doanhThuTheoGio(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                gio.getThoiGian()
                        );

                row.createCell(1)
                        .setCellValue(
                                gio.getSoHoaDon()
                        );

                row.createCell(2)
                        .setCellValue(
                                gio.getDoanhThu()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 12. DANH MỤC
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "12. DOANH THU THEO DANH MỤC",
                    titleStyle
            );

            Row dmHeader =
                    sheet.createRow(rowIndex++);

            String[] dmColumns = {
                    "Danh mục",
                    "Số hóa đơn",
                    "Số lượng bán",
                    "Tổng thu"
            };

            for (int i = 0; i < dmColumns.length; i++) {

                dmHeader.createCell(i)
                        .setCellValue(dmColumns[i]);

                dmHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeDanhMuc dm :
                    doanhThuTheoDanhMuc(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                dm.getDanhMuc()
                        );

                row.createCell(1)
                        .setCellValue(
                                dm.getSoHoaDon()
                        );

                row.createCell(2)
                        .setCellValue(
                                dm.getSoLuongBan()
                        );

                row.createCell(3)
                        .setCellValue(
                                dm.getTongThu()
                        );
            }

            rowIndex += 2;

            // =====================================================
            // 13. HIỆU SUẤT BÀN
            // =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "13. HIỆU SUẤT BÀN",
                    titleStyle
            );

            Row banHeader =
                    sheet.createRow(rowIndex++);

            String[] banColumns = {
                    "Bàn",
                    "Khu vực",
                    "Số lần phục vụ",
                    "Doanh thu"
            };

            for (int i = 0; i < banColumns.length; i++) {

                banHeader.createCell(i)
                        .setCellValue(banColumns[i]);

                banHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeHieuSuatBan ban :
                    hieuSuatBan(from, to)) {

                Row row =
                        sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                ban.getTenBan()
                        );

                row.createCell(1)
                        .setCellValue(
                                ban.getKhuVuc()
                        );

                row.createCell(2)
                        .setCellValue(
                                ban.getSoLanPhucVu()
                        );

                row.createCell(3)
                        .setCellValue(
                                ban.getTongDoanhThu()
                        );
            }

            rowIndex += 2;

// =====================================================
// 14. TỶ LỆ HỦY ĐẶT BÀN
// =====================================================

            rowIndex = writeTitle(
                    sheet,
                    rowIndex,
                    "14. TỶ LỆ HỦY ĐẶT BÀN",
                    titleStyle
            );

            Row huyHeader = sheet.createRow(rowIndex++);

            String[] huyColumns = {
                    "Trạng thái",
                    "Số lượng",
                    "Tổng tiền cọc"
            };

            for (int i = 0; i < huyColumns.length; i++) {

                huyHeader.createCell(i)
                        .setCellValue(huyColumns[i]);

                huyHeader.getCell(i)
                        .setCellStyle(headerStyle);
            }

            for (DTOThongKeHuyDatBan huy :
                    tyLeHuyDatBan(from, to)) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0)
                        .setCellValue(
                                huy.getTrangThai() != null
                                        ? huy.getTrangThai()
                                        : ""
                        );

                row.createCell(1)
                        .setCellValue(
                                huy.getSoLuong() != null
                                        ? huy.getSoLuong()
                                        : 0L
                        );

                row.createCell(2)
                        .setCellValue(
                                huy.getTongTienCoc() != null
                                        ? huy.getTongTienCoc()
                                        : 0.0
                        );
            }

            rowIndex += 2;

            // =====================================================
            // AUTO SIZE
            // =====================================================

            for (int i = 0; i < 9; i++) {

                sheet.autoSizeColumn(i);

                // tránh cột quá rộng
                int width =
                        sheet.getColumnWidth(i);

                if (width > 15000) {
                    sheet.setColumnWidth(
                            i,
                            15000
                    );
                }
            }

            // =====================================================
            // WRITE FILE
            // =====================================================

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể tạo file Excel",
                    e
            );
        }
    }

    // =========================================================
    // WRITE TITLE
    // =========================================================

    private int writeTitle(
            Sheet sheet,
            int rowIndex,
            String title,
            CellStyle style
    ) {

        Row row =
                sheet.createRow(rowIndex++);

        Cell cell =
                row.createCell(0);

        cell.setCellValue(title);
        cell.setCellStyle(style);

        return rowIndex;
    }

    // =========================================================
    // WRITE DOANH THU
    // =========================================================

    private int writeDoanhThu(
            Sheet sheet,
            int rowIndex,
            List<DTOThongKeDoanhThu> data,
            CellStyle headerStyle
    ) {

        Row header =
                sheet.createRow(rowIndex++);

        String[] columns = {
                "Thời gian",
                "Số hóa đơn",
                "Tổng tiền",
                "Giảm giá",
                "Doanh thu"
        };

        for (int i = 0; i < columns.length; i++) {

            header.createCell(i)
                    .setCellValue(columns[i]);

            header.getCell(i)
                    .setCellStyle(headerStyle);
        }

        for (DTOThongKeDoanhThu item : data) {

            Row row =
                    sheet.createRow(rowIndex++);

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

    // =========================================================
    // HELPER
    // =========================================================

    private String stringValue(
            Object[] row,
            int index
    ) {

        if (row == null ||
                index >= row.length ||
                row[index] == null) {

            return "";
        }

        return row[index].toString();
    }

    private Long longValue(
            Object[] row,
            int index
    ) {

        if (row == null ||
                index >= row.length ||
                row[index] == null) {

            return 0L;
        }

        return parseLongSafe(row[index]);
    }

    private Integer intValue(
            Object[] row,
            int index
    ) {

        return longValue(row, index).intValue();
    }

    private Double doubleValue(
            Object[] row,
            int index
    ) {

        if (row == null ||
                index >= row.length ||
                row[index] == null) {

            return 0.0;
        }

        try {

            return Double.valueOf(
                    row[index].toString()
            );

        } catch (NumberFormatException e) {

            return 0.0;
        }
    }

    private Long parseLongSafe(
            Object value
    ) {

        if (value == null) {
            return 0L;
        }

        try {

            String s =
                    value.toString().trim();

            if (s.isEmpty()) {
                return 0L;
            }

            return Math.round(
                    Double.parseDouble(s)
            );

        } catch (NumberFormatException e) {

            return 0L;
        }
    }
}
