package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;


import java.util.List;

public interface ThongKeService {
    List<DTOThongKeDoanhThu> theoNgay(String from, String to);

    List<DTOThongKeDoanhThu> theoThang();

    List<DTOThongKeDoanhThu> theoNam();

    List<DTOThongKeNhanVien> topNhanVien();

    List<DTOThongKeTheoMon> topMon(int page, int size);

    DTODashboard dashboard();
    List<DTOThongKeDoanhThu> tienCocTheoNgay();

    List<DTOTrangThaiCoc> trangThaiCoc();

    List<DTOThongKeKhachHangThanThiet> topKhachHangThanThiet();

    // 2. Doanh thu theo khu vực
    List<DTOThongKeKhuVuc> doanhThuTheoKhuVuc();

    // 3. Hiệu suất bàn
    List<DTOThongKeHieuSuatBan> hieuSuatBan();

    // 4. Top sản phẩm bán chạy (cả món & combo)
    List<DTOThongKeSanPhamBanChay> topSanPhamBanChay();

    // 5. Hiệu quả khuyến mãi
    List<DTOThongKeKhuyenMai> hieuQuaKhuyenMai();

    // 6. Doanh thu theo giờ (phân tích giờ cao điểm)
    List<DTOThongKeDoanhThu> doanhThuTheoGio();




    // 8. Tỉ lệ hủy đặt bàn
    List<DTOThongKeHuyDatBan> tyLeHuyDatBan(String fromDate, String toDate);
    List<DTOThongKeDanhMuc> doanhThuTheoDanhMuc();

}
