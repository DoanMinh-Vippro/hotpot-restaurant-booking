package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {
    @Query(value = """
        SELECT 
            CONVERT(varchar, thoi_gian_xuat, 23) as thoiGian,
            SUM(tong_tien) as doanhThu
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
          AND thoi_gian_xuat BETWEEN :from AND :to
        GROUP BY CONVERT(varchar, thoi_gian_xuat, 23)
        ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoNgay(String from, String to);


    // ===== 2. Theo tháng =====
    @Query(value = """
        SELECT 
            FORMAT(thoi_gian_xuat, 'yyyy-MM') as thoiGian,
            SUM(tong_tien) as doanhThu
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
        GROUP BY FORMAT(thoi_gian_xuat, 'yyyy-MM')
        ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoThang();


    // ===== 3. Theo năm =====
    @Query(value = """
        SELECT 
            YEAR(thoi_gian_xuat) as thoiGian,
            SUM(tong_tien) as doanhThu
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
        GROUP BY YEAR(thoi_gian_xuat)
        ORDER BY thoiGian
    """, nativeQuery = true)
    List<Object[]> thongKeTheoNam();


    // ===== 4. Top nhân viên =====
    @Query(value = """
        SELECT 
            nv.ten_nhan_vien,
            SUM(hd.tong_tien)
        FROM HoaDon hd
        JOIN NhanVien nv ON hd.id_nhan_vien = nv.id_nhan_vien
        WHERE hd.trang_thai_thanh_toan = 1
        GROUP BY nv.ten_nhan_vien
        ORDER BY SUM(hd.tong_tien) DESC
    """, nativeQuery = true)
    List<Object[]> topNhanVien();


    // ===== 5. Top món =====
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


    // ===== 6. Dashboard =====
    @Query(value = """
        SELECT 
            SUM(tong_tien),
            COUNT(*),
            (SELECT COUNT(*) FROM KhachHang)
        FROM HoaDon
        WHERE trang_thai_thanh_toan = 1
    """, nativeQuery = true)
    Object dashboard();
}
