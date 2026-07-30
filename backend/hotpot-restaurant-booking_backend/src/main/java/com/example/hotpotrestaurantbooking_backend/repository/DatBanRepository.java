package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public interface DatBanRepository extends JpaRepository<DatBan,Integer> {
    List<DatBan> findByTrangThaiCoc(Integer trangThaiCoc);

    // ĐỔI TÊN HÀM NÀY: Từ findByKhachHangId thành findByKhachHangIdKhachHang
    List<DatBan> findByKhachHangIdKhachHang(Integer khachHangId);

    List<DatBan> findBySoTienCocGreaterThan(BigDecimal soTien);
    // THÊM CÂU QUERY THẦN THÁNH NÀY: Tính tổng tiền trực tiếp bằng SQL/HQL
    // Nó tự check nếu chuỗi Enum tên là 'DA_COC' hoặc 'DA_THANH_TOAN' thì cộng vào
    @Query("SELECT COALESCE(SUM(d.soTienCoc), 0) FROM DatBan d " +
            "WHERE d.trangThaiCoc = com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc.DA_COC")
    BigDecimal sumTongTienCocDaThu();

    //===============================================================
    List<DatBan> findByKhachHang_IdKhachHang(Integer id);

    List<DatBan> findByTrangThai(TrangThaiDatBan trangThai);


//    List<DatBan> findByTrangThaiIn(List<TrangThaiDatBan> trangThais);

    List<DatBan> findByTrangThaiIn(List<TrangThaiDatBan> trangThai);

    @Query("""
select distinct d
from DatBan d
join d.chiTietDatBanBans ct
where ct.ban.idBan = :idBan
and d.trangThai in :trangThai
""")
    List<DatBan> findByBanAndTrangThai(Integer idBan, List<TrangThaiDatBan> trangThai);

    @Query("""
    SELECT DISTINCT d
    FROM DatBan d
    JOIN d.chiTietDatBanBans ct
    WHERE ct.ban.idBan = :idBan
      AND d.trangThai IN :trangThai
""")
    List<DatBan> findByIdBanAndTrangThai(Integer idBan, List<TrangThaiDatBan> trangThai);


    @Query("""
SELECT d
FROM DatBan d
WHERE (:tuNgay IS NULL OR d.thoiGianDenDuKien >= :tuNgay)
AND (:denNgay IS NULL OR d.thoiGianDenDuKien <= :denNgay)
ORDER BY d.thoiGianDenDuKien DESC
""")
    List<DatBan> findByThoiGianDenDuKienBetween(
            LocalDateTime tuNgay,
            LocalDateTime denNgay
    );

    @Query("""
SELECT d
FROM DatBan d
LEFT JOIN d.khachHang k
WHERE
      LOWER(k.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
   OR d.sdtKhachHang LIKE CONCAT('%', :keyword, '%')
ORDER BY d.thoiGianDenDuKien DESC
""")
    List<DatBan> searchByKeyword(String keyword);
}
