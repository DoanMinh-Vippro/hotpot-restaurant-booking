package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    // ========================
    // 1. DOANH THU THEO NGÀY (CÓ LỌC KHOẢNG) - CHI TIẾT
    // ========================
    @Query(value = """
        SELECT 
            CONVERT(varchar, thoi_gian_xuat, 23) AS ngay,
            COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
            ISNULL(SUM(hd.tong_tien), 0) AS doanhThu,
            ISNULL(SUM(hd.tien_giam_gia), 0) AS tongGiamGia,
            ISNULL(SUM(hd.tien_coc), 0) AS tongTienCoc
        FROM HoaDon hd
        WHERE hd.trang_thai_thanh_toan = 1
          AND hd.thoi_gian_xuat BETWEEN :fromDate AND :toDate
        GROUP BY CONVERT(varchar, hd.thoi_gian_xuat, 23)
        ORDER BY ngay
    """, nativeQuery = true)
    List<Object[]> doanhThuTheoNgay(String fromDate, String toDate);

    // ========================
    // 2. DOANH THU THEO THÁNG
    // ========================
    @Query(value = """
        SELECT 
            FORMAT(thoi_gian_xuat, 'yyyy-MM') as thoiGian,
            ISNULL(SUM(tong_tien), 0) as doanhThu
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
        GROUP BY FORMAT(thoi_gian_xuat, 'yyyy-MM')
        ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoThang();

    // ========================
    // 3. DOANH THU THEO NĂM
    // ========================
    @Query(value = """
        SELECT 
            YEAR(thoi_gian_xuat) as thoiGian,
            ISNULL(SUM(tong_tien), 0) as doanhThu
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
        GROUP BY YEAR(thoi_gian_xuat)
        ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoNam();

    // ========================
    // 4. TOP NHÂN VIÊN
    // ========================
    @Query(value = """
        SELECT 
            nv.ten_nhan_vien,
            ISNULL(SUM(hd.tong_tien), 0)
        FROM HoaDon hd
        JOIN NhanVien nv ON hd.id_nhan_vien = nv.id_nhan_vien
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY nv.ten_nhan_vien
        ORDER BY SUM(hd.tong_tien) DESC
    """, nativeQuery = true)
    List<Object[]> topNhanVien();

    // ========================
    // 5. TOP MÓN (có pagination) - GIỮ LẠI METHOD CŨ
    // ========================
    @Query(value = """
        SELECT 
            m.ten_mon,
            SUM(hdct.so_luong)
        FROM HoaDonChiTiet hdct
        JOIN Mon m ON hdct.id_mon = m.id_mon
        GROUP BY m.ten_mon
        ORDER BY SUM(hdct.so_luong) DESC
    """, nativeQuery = true)
    List<Object[]> topMon(Pageable pageable);

    // ========================
    // 6. TOP SẢN PHẨM BÁN CHẠY (MÓN + COMBO)
    // ========================
    @Query(value = """
        SELECT TOP 10
            N'MÓN' AS loai,
            m.ten_mon AS tenSanPham,
            ISNULL(SUM(hdct.so_luong), 0) AS soLuongBan,
            ISNULL(SUM(hdct.thanh_tien), 0) AS tongThu
        FROM HoaDonChiTiet hdct
        JOIN Mon m ON hdct.id_mon = m.id_mon
        JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY m.ten_mon
        
        UNION ALL
        
        SELECT 
            N'COMBO' AS loai,
            c.ten_combo AS tenSanPham,
            ISNULL(SUM(hdct.so_luong), 0) AS soLuongBan,
            ISNULL(SUM(hdct.thanh_tien), 0) AS tongThu
        FROM HoaDonChiTiet hdct
        JOIN Combo c ON hdct.id_combo = c.id_combo
        JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY c.ten_combo
        
        ORDER BY soLuongBan DESC
    """, nativeQuery = true)
    List<Object[]> topSanPhamBanChay();

    // ========================
    // 7. TIỀN CỌC THEO NGÀY
    // ========================
    @Query(value = """
        SELECT
            CONVERT(varchar, thoi_gian_den_du_kien, 23),
            ISNULL(SUM(so_tien_coc), 0)
        FROM DatBan
        WHERE trang_thai_coc = 1
        GROUP BY CONVERT(varchar, thoi_gian_den_du_kien, 23)
        ORDER BY 1
    """, nativeQuery = true)
    List<Object[]> tienCocTheoNgay();

    // ========================
    // 8. TRẠNG THÁI CỌC
    // ========================
    @Query(value = """
        SELECT
            trang_thai_coc,
            COUNT(*)
        FROM DatBan
        GROUP BY trang_thai_coc
        ORDER BY trang_thai_coc
    """, nativeQuery = true)
    List<Object[]> trangThaiCoc();

    // ========================
    // 9. DASHBOARD TỔNG QUAN (GIỮ LẠI METHOD CŨ)
    // ========================
    @Query(value = """
    SELECT
        ISNULL(SUM(hd.tong_tien), 0) AS tongDoanhThu,
        COUNT(hd.id_hoa_don) AS tongHoaDon,
        (SELECT COUNT(*) FROM KhachHang) AS tongKhachHang,
        (SELECT ISNULL(SUM(so_tien_coc), 0) FROM DatBan WHERE trang_thai_coc = 1) AS tongTienCoc,
        (SELECT COUNT(*) FROM DatBan WHERE trang_thai_coc = 1) AS soDonDaCoc,
        (SELECT COUNT(*) FROM DatBan WHERE trang_thai_coc = 0) AS soDonChuaCoc,
        (SELECT COUNT(*) FROM DatBan WHERE trang_thai_coc = 2) AS soDonHoanCoc
    FROM HoaDon hd
    WHERE hd.trang_thai_thanh_toan = 1
""", nativeQuery = true)
    Object dashboard();
    // ========================
    // 10. DOANH THU THEO KHU VỰC
    // ========================
    @Query(value = """
        SELECT 
            kv.ten_khu_vuc AS khuVuc,
            COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
            ISNULL(SUM(hd.tong_tien), 0) AS doanhThu,
            ISNULL(AVG(hd.tong_tien), 0) AS trungBinhHoaDon
        FROM HoaDon hd
        JOIN Ban b ON hd.id_ban = b.id_ban
        JOIN KhuVuc kv ON b.id_khu_vuc = kv.id_khu_vuc
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY kv.ten_khu_vuc
        ORDER BY doanhThu DESC
    """, nativeQuery = true)
    List<Object[]> doanhThuTheoKhuVuc();

    @Query(value = """
        SELECT TOP 10
            kh.ten_khach_hang AS tenKhachHang,
            kh.so_dien_thoai AS soDienThoai,
            COUNT(DISTINCT hd.id_hoa_don) AS soLanDen,
            SUM(hd.tong_tien) AS tongChiTieu,
            AVG(hd.tong_tien) AS trungBinhHoaDon,
            MAX(hd.thoi_gian_xuat) AS lanCuoiDen
        FROM HoaDon hd
        JOIN KhachHang kh ON hd.id_khach_hang = kh.id_khach_hang
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY kh.ten_khach_hang, kh.so_dien_thoai
        ORDER BY tongChiTieu DESC
    """, nativeQuery = true)
    List<Object[]> topKhachHangThanThiet();
    // ========================
    // 11. HIỆU SUẤT BÀN
    // ========================
    @Query(value = """
        SELECT 
            b.ten_ban AS tenBan,
            kv.ten_khu_vuc AS khuVuc,
            COUNT(DISTINCT hd.id_hoa_don) AS soLanPhucVu,
            ISNULL(SUM(hd.tong_tien), 0) AS tongDoanhThu,
            ISNULL(AVG(hd.tong_tien), 0) AS doanhThuTrungBinh
        FROM HoaDon hd
        JOIN Ban b ON hd.id_ban = b.id_ban
        JOIN KhuVuc kv ON b.id_khu_vuc = kv.id_khu_vuc
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY b.ten_ban, kv.ten_khu_vuc
        ORDER BY tongDoanhThu DESC
    """, nativeQuery = true)
    List<Object[]> hieuSuatBan();

    // ========================
    // 12. HIỆU QUẢ KHUYẾN MÃI
    // ========================
    @Query(value = """
        SELECT 
            gg.ma_giam_gia AS maGiamGia,
            gg.loai_giam AS loaiGiam,
            gg.gia_tri_giam AS giaTriGiam,
            COUNT(hd.id_hoa_don) AS soLanSuDung,
            ISNULL(SUM(hd.tien_giam_gia), 0) AS tongTienDaGiam,
            ISNULL(SUM(hd.tong_tien), 0) AS tongDoanhThuSauGiam
        FROM HoaDon hd
        JOIN GiamGia gg ON hd.id_giam_gia = gg.id_giam_gia
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY gg.ma_giam_gia, gg.loai_giam, gg.gia_tri_giam
        ORDER BY soLanSuDung DESC
    """, nativeQuery = true)
    List<Object[]> hieuQuaKhuyenMai();

    // ========================
    // 13. DOANH THU THEO GIỜ
    // ========================
    @Query(value = """
        SELECT 
            DATEPART(HOUR, thoi_gian_xuat) AS khungGio,
            COUNT(DISTINCT id_hoa_don) AS soLuongHoaDon,
            ISNULL(SUM(tong_tien), 0) AS doanhThu,
            ISNULL(AVG(tong_tien), 0) AS trungBinhHoaDon
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
        GROUP BY DATEPART(HOUR, thoi_gian_xuat)
        ORDER BY khungGio
    """, nativeQuery = true)
    List<Object[]> doanhThuTheoGio();

    // ========================
    // 15. TỈ LỆ HỦY ĐẶT BÀN
    // ========================
    @Query(value = """
        SELECT 
            CASE 
                WHEN db.trang_thai = 0 THEN N'Đã hủy'
                WHEN db.trang_thai = 1 THEN N'Đã đặt'
                WHEN db.trang_thai = 2 THEN N'Đã nhận bàn'
                WHEN db.trang_thai = 3 THEN N'Không đến'
                ELSE N'Khác'
            END AS trangThaiDatBan,
            COUNT(*) AS soLuong,
            ISNULL(SUM(db.so_tien_coc), 0) AS tongTienCoc
        FROM DatBan db
        WHERE db.ngay_dat BETWEEN :fromDate AND :toDate
        GROUP BY db.trang_thai
        ORDER BY soLuong DESC
    """, nativeQuery = true)
    List<Object[]> tyLeHuyDatBan(String fromDate, String toDate);

    // ========================
    // 16. DOANH THU THEO DANH MỤC
    // ========================
    @Query(value = """
        SELECT 
            dm.loai_danh_muc AS danhMuc,
            COUNT(DISTINCT hdct.id_hoa_don) AS soHoaDon,
            ISNULL(SUM(hdct.so_luong), 0) AS soLuongBan,
            ISNULL(SUM(hdct.thanh_tien), 0) AS tongThu,
            ISNULL(AVG(hdct.gia_ban_tai_thoi_dien), 0) AS giaTrungBinh
        FROM HoaDonChiTiet hdct
        JOIN Mon m ON hdct.id_mon = m.id_mon
        JOIN DanhMuc dm ON m.id_danh_muc = dm.id_danh_muc
        JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY dm.loai_danh_muc
        ORDER BY tongThu DESC
    """, nativeQuery = true)
    List<Object[]> doanhThuTheoDanhMuc();
}
