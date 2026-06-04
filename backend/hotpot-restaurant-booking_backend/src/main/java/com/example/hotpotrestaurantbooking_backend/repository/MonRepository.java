package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MonRepository extends JpaRepository<Mon,Integer> {
    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.MonResponse(m.idMon, m.tenMon, m.donGiaHienTai, m.danhMuc.idDanhMuc,m.danhMuc.loaiDanhMuc, m.trangThai)
    from Mon m join DanhMuc dm on m.danhMuc.idDanhMuc=dm.idDanhMuc
""")
    List<MonResponse>hienThiMon();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.MonResponse(m.idMon, m.tenMon, m.donGiaHienTai, m.danhMuc.idDanhMuc,m.danhMuc.loaiDanhMuc, m.trangThai)
    from Mon m join DanhMuc dm on m.danhMuc.idDanhMuc=dm.idDanhMuc where m.tenMon=?1
""")
    MonResponse detailMon(String tenMon);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.MonResponse(m.idMon, m.tenMon, m.donGiaHienTai, m.danhMuc.idDanhMuc,m.danhMuc.loaiDanhMuc, m.trangThai)
    from Mon m join DanhMuc dm on m.danhMuc.idDanhMuc=dm.idDanhMuc
""")
    Page<MonResponse> phanTrangMon(Pageable pageable);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.MonResponse(
        m.idMon,
        m.tenMon,
        m.donGiaHienTai,
m.danhMuc.idDanhMuc,        m.danhMuc.loaiDanhMuc, m.trangThai
    )
    from Mon m join DanhMuc dm on m.danhMuc.idDanhMuc=dm.idDanhMuc
    where
        (:tenMon is null or lower(m.tenMon) like lower(concat('%', :tenMon, '%')))
    and
        (:giaMin is null or m.donGiaHienTai >= :giaMin)
    and
        (:giaMax is null or m.donGiaHienTai <= :giaMax)
    and
        (:loaiDanhMuc is null or lower(m.danhMuc.loaiDanhMuc)
                 like lower(concat('%', :loaiDanhMuc, '%')))
""")
    Page<MonResponse> timKiemMon(
            @Param("tenMon") String tenMon,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("loaiDanhMuc") String loaiDanhMuc,
            Pageable pageable
    );

    boolean existsByTenMonIgnoreCase(String tenMon);
    Mon findByTenMonIgnoreCase(String tenMon);
    Mon findByIdMon(Integer idMon);
}