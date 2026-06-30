package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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
    List<DatBan> findByKhachHang_IdKhachHang(Integer id);

}
