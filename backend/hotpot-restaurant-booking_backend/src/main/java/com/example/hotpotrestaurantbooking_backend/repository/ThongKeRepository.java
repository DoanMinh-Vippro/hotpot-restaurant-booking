package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    // ========================
    // 1. DOANH THU THEO NGÀY (CÓ LỌC KHOẢNG) - CHI TIẾT
    // ========================
    @Query(value = """
    SELECT 
        CONVERT(varchar, hd.thoi_gian_xuat, 23) AS ngay,
        COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
        ISNULL(SUM(hd.tong_tien), 0) AS doanhThu,
        ISNULL(SUM(hd.tien_giam_gia), 0) AS tongGiamGia,
        ISNULL(SUM(hd.tien_coc), 0) AS tongTienCoc
    FROM HoaDon hd
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY CONVERT(varchar, hd.thoi_gian_xuat, 23)
    ORDER BY ngay
""", nativeQuery = true)
    List<Object[]> doanhThuTheoNgay(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
    // 2. DOANH THU THEO THÁNG
    // ========================
    @Query(value = """
    SELECT 
        FORMAT(hd.thoi_gian_xuat, 'yyyy-MM') AS thoiGian,
        COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
        ISNULL(SUM(hd.tong_tien), 0) AS tongTien,
        ISNULL(SUM(hd.tien_giam_gia), 0) AS giamGia,
        ISNULL(SUM(hd.tong_tien), 0) AS doanhThu
    FROM HoaDon hd
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY FORMAT(hd.thoi_gian_xuat, 'yyyy-MM')
    ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoThang(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
    // 3. DOANH THU THEO NĂM
    // ========================
    @Query(value = """
    SELECT 
        YEAR(hd.thoi_gian_xuat) AS thoiGian,
        COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
        ISNULL(SUM(hd.tong_tien), 0) AS tongTien,
        ISNULL(SUM(hd.tien_giam_gia), 0) AS giamGia,
        ISNULL(SUM(hd.tong_tien), 0) AS doanhThu
    FROM HoaDon hd
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY YEAR(hd.thoi_gian_xuat)
    ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoNam(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

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
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY nv.ten_nhan_vien
    ORDER BY SUM(hd.tong_tien) DESC
""", nativeQuery = true)
    List<Object[]> topNhanVien(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
    // 5. TOP MÓN (có pagination) - GIỮ LẠI METHOD CŨ
    // ========================
    @Query(value = """ 
SELECT m.ten_mon, SUM(hdct.so_luong) FROM HoaDonChiTiet hdct JOIN Mon m ON hdct.id_mon = m.id_mon JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don WHERE hd.trang_thai_thanh_toan = 1 AND hd.thoi_gian_xuat >= :fromDate AND hd.thoi_gian_xuat < :toDate GROUP BY m.ten_mon ORDER BY SUM(hdct.so_luong) DESC """, nativeQuery = true) List<Object[]> topMon( @Param("fromDate") String fromDate, @Param("toDate") String toDate, Pageable pageable
    );
    // ========================
    // 6. TOP SẢN PHẨM BÁN CHẠY (MÓN + COMBO)
    // ========================
    @Query(value = """
    SELECT TOP 10
        N'MÓN',
        m.ten_mon,
        ISNULL(SUM(hdct.so_luong), 0),
        ISNULL(SUM(hdct.thanh_tien), 0)
    FROM HoaDonChiTiet hdct
    JOIN Mon m ON hdct.id_mon = m.id_mon
    JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY m.ten_mon

    UNION ALL

    SELECT
        N'COMBO',
        c.ten_combo,
        ISNULL(SUM(hdct.so_luong), 0),
        ISNULL(SUM(hdct.thanh_tien), 0)
    FROM HoaDonChiTiet hdct
    JOIN Combo c ON hdct.id_combo = c.id_combo
    JOIN HoaDon hd ON hdct.id_hoa_don = hd.id_hoa_don
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY c.ten_combo

    ORDER BY 3 DESC
""", nativeQuery = true)
    List<Object[]> topSanPhamBanChay(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
    // 7. TIỀN CỌC THEO NGÀY
    // ========================
    @Query(value = """
    SELECT
        CONVERT(varchar, db.thoi_gian_den_du_kien, 23),
        ISNULL(SUM(db.so_tien_coc), 0)
    FROM DatBan db
    WHERE db.trang_thai_coc = 1
      AND db.thoi_gian_den_du_kien >= :fromDate
      AND db.thoi_gian_den_du_kien < :toDate
    GROUP BY CONVERT(varchar, db.thoi_gian_den_du_kien, 23)
    ORDER BY 1
""", nativeQuery = true)
    List<Object[]> tienCocTheoNgay(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
    // 8. TRẠNG THÁI CỌC
    @Query(value = """
    SELECT
        db.trang_thai_coc,
        COUNT(*)
    FROM DatBan db
    WHERE db.thoi_gian_den_du_kien >= :fromDate
      AND db.thoi_gian_den_du_kien < :toDate
    GROUP BY db.trang_thai_coc
    ORDER BY db.trang_thai_coc
""", nativeQuery = true)
    List<Object[]> trangThaiCoc(
                    @Param("fromDate") String fromDate,
                    @Param("toDate") String toDate
            );
// 9. DASHBOARD TỔNG QUAN
// ========================
@Query(value = """
    SELECT
        -- 0. Tổng doanh thu
        ISNULL(SUM(hd.tong_tien), 0),

        -- 1. Doanh thu tiền mặt
        ISNULL(SUM(
            CASE
                WHEN hd.phuong_thuc_thanh_toan = 1
                THEN hd.tong_tien
                ELSE 0
            END
        ), 0),

        -- 2. Doanh thu chuyển khoản
        ISNULL(SUM(
            CASE
                WHEN hd.phuong_thuc_thanh_toan = 2
                THEN hd.tong_tien
                ELSE 0
            END
        ), 0),

        -- 3. Tổng hóa đơn
        COUNT(hd.id_hoa_don),

        -- 4. Tổng khách hàng
        (
            SELECT COUNT(*)
            FROM KhachHang
        ),

       -- 5. Tiền cọc tiền mặt
          (
              SELECT ISNULL(SUM(hd2.tien_coc), 0)
              FROM HoaDon hd2
              WHERE hd2.tien_coc > 0
                AND hd2.phuong_thuc_thanh_toan = 1
                AND hd2.trang_thai_thanh_toan = 1
                AND hd2.thoi_gian_xuat >= :fromDate
                AND hd2.thoi_gian_xuat < DATEADD(DAY, 1, CAST(:toDate AS DATE))
          ),
          
          -- 6. Tiền cọc chuyển khoản
          (
              SELECT ISNULL(SUM(hd2.tien_coc), 0)
              FROM HoaDon hd2
              WHERE hd2.tien_coc > 0
                AND hd2.phuong_thuc_thanh_toan = 2
                AND hd2.trang_thai_thanh_toan = 1
                AND hd2.thoi_gian_xuat >= :fromDate
                AND hd2.thoi_gian_xuat < DATEADD(DAY, 1, CAST(:toDate AS DATE))
          ),
          
          -- 7. Số hóa đơn đã cọc
          (
              SELECT COUNT(*)
              FROM HoaDon hd2
              WHERE hd2.tien_coc > 0
                AND hd2.trang_thai_thanh_toan = 1
                AND hd2.thoi_gian_xuat >= :fromDate
                AND hd2.thoi_gian_xuat < DATEADD(DAY, 1, CAST(:toDate AS DATE))
          ),
          
          -- 8. Số hóa đơn chưa cọc
          (
              SELECT COUNT(*)
              FROM HoaDon hd2
              WHERE (hd2.tien_coc IS NULL OR hd2.tien_coc = 0)
                AND hd2.trang_thai_thanh_toan = 1
                AND hd2.thoi_gian_xuat >= :fromDate
                AND hd2.thoi_gian_xuat < DATEADD(DAY, 1, CAST(:toDate AS DATE))
          ),

        -- 9. Số hóa đơn đã thanh toán
        COUNT(hd.id_hoa_don)

    FROM HoaDon hd

    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < DATEADD(DAY, 1, CAST(:toDate AS DATE))
""", nativeQuery = true)
Object dashboard(
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
);
    // ========================
    // 10. DOANH THU THEO KHU VỰC
    // ========================
    @Query(value = """
    SELECT 
        kv.ten_khu_vuc,
        COUNT(DISTINCT hd.id_hoa_don),
        ISNULL(SUM(hd.tong_tien), 0),
        ISNULL(AVG(hd.tong_tien), 0)
    FROM HoaDon hd
    JOIN Ban b ON hd.id_ban = b.id_ban
    JOIN KhuVuc kv ON b.id_khu_vuc = kv.id_khu_vuc
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY kv.ten_khu_vuc
    ORDER BY SUM(hd.tong_tien) DESC
""", nativeQuery = true)
    List<Object[]> doanhThuTheoKhuVuc(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    @Query(value = """
    SELECT TOP 10
        kh.ten_khach_hang,
        kh.so_dien_thoai,
        COUNT(DISTINCT hd.id_hoa_don),
        SUM(hd.tong_tien),
        AVG(hd.tong_tien),
        MAX(hd.thoi_gian_xuat)
    FROM HoaDon hd
    JOIN KhachHang kh 
        ON hd.id_khach_hang = kh.id_khach_hang
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY 
        kh.ten_khach_hang,
        kh.so_dien_thoai
    ORDER BY SUM(hd.tong_tien) DESC
""", nativeQuery = true)
    List<Object[]> topKhachHangThanThiet(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );
    // ========================
    // 11. HIỆU SUẤT BÀN
    // ========================
    @Query(value = """
    SELECT 
        b.ten_ban,
        kv.ten_khu_vuc,
        COUNT(DISTINCT hd.id_hoa_don),
        ISNULL(SUM(hd.tong_tien), 0),
        ISNULL(AVG(hd.tong_tien), 0)
    FROM HoaDon hd
    JOIN Ban b ON hd.id_ban = b.id_ban
    JOIN KhuVuc kv ON b.id_khu_vuc = kv.id_khu_vuc
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY b.ten_ban, kv.ten_khu_vuc
    ORDER BY SUM(hd.tong_tien) DESC
""", nativeQuery = true)
    List<Object[]> hieuSuatBan(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );
    // ========================
    // 12. HIỆU QUẢ KHUYẾN MÃI
    // ========================
    @Query(value = """
    SELECT 
        gg.ma_giam_gia,
        gg.loai_giam,
        gg.gia_tri_giam,
        COUNT(hd.id_hoa_don),
        ISNULL(SUM(hd.tien_giam_gia), 0),
        ISNULL(SUM(hd.tong_tien), 0)
    FROM HoaDon hd
    JOIN GiamGia gg ON hd.id_giam_gia = gg.id_giam_gia
    WHERE hd.trang_thai_thanh_toan = 1
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY 
        gg.ma_giam_gia,
        gg.loai_giam,
        gg.gia_tri_giam
    ORDER BY COUNT(hd.id_hoa_don) DESC
""", nativeQuery = true)
    List<Object[]> hieuQuaKhuyenMai(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    // ========================
// 13. DOANH THU THEO GIỜ
// ========================
    @Query(value = """
    SELECT  
        h.gio,
        COUNT(DISTINCT hd.id_hoa_don) AS soHoaDon,
        ISNULL(SUM(hd.tong_tien), 0) AS doanhThu,
        ISNULL(AVG(hd.tong_tien), 0) AS trungBinh
    FROM (
        SELECT 0 AS gio
        UNION ALL SELECT 1
        UNION ALL SELECT 2
        UNION ALL SELECT 3
        UNION ALL SELECT 4
        UNION ALL SELECT 5
        UNION ALL SELECT 6
        UNION ALL SELECT 7
        UNION ALL SELECT 8
        UNION ALL SELECT 9
        UNION ALL SELECT 10
        UNION ALL SELECT 11
        UNION ALL SELECT 12
        UNION ALL SELECT 13
        UNION ALL SELECT 14
        UNION ALL SELECT 15
        UNION ALL SELECT 16
        UNION ALL SELECT 17
        UNION ALL SELECT 18
        UNION ALL SELECT 19
        UNION ALL SELECT 20
        UNION ALL SELECT 21
        UNION ALL SELECT 22
        UNION ALL SELECT 23
    ) h
    LEFT JOIN HoaDon hd
        ON DATEPART(HOUR, hd.thoi_gian_xuat) = h.gio
        AND hd.trang_thai_thanh_toan = 1
        AND hd.thoi_gian_xuat >= :fromDate
        AND hd.thoi_gian_xuat < :toDate
    GROUP BY h.gio
    ORDER BY h.gio
""", nativeQuery = true)
    List<Object[]> doanhThuTheoGio(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

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
    List<Object[]> tyLeHuyDatBan(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

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
      AND hd.thoi_gian_xuat >= :fromDate
      AND hd.thoi_gian_xuat < :toDate
    GROUP BY dm.loai_danh_muc
    ORDER BY tongThu DESC
""", nativeQuery = true)
    List<Object[]> doanhThuTheoDanhMuc(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );
}
