package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ChiTietGiamGiaMonRepository extends JpaRepository<ChiTietGiamGiaMon,Integer> {
    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.idDotGiamGia, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.idMon, ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon
""")
    List<ChiTietGiamGiaMonResponse> hienThiCTGGM();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.idDotGiamGia, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.idMon, ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon 
    where ctggm.idChiTietGiamGiaMon=?1
""")
    ChiTietGiamGiaMonResponse detailCTGGM(Integer idChiTietGiamGiaMon);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.idDotGiamGia, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.idMon, ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon
""")
    Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(Pageable pageable);

    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
            ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.idDotGiamGia, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.idMon, ctggm.mon.tenMon, ctggm.mucGiam
        )
        from ChiTietGiamGiaMon ctggm 
        join ctggm.dotGiamGia dgg 
        join ctggm.mon m 
        where
            (:tenChuongTrinh is null or :tenChuongTrinh = '' or lower(dgg.tenChuongTrinh) like lower(concat('%', :tenChuongTrinh, '%')))
        and
            (:tenMon is null or :tenMon = '' or lower(m.tenMon) like lower(concat('%', :tenMon, '%')))
        and
            (:mucMin is null or ctggm.mucGiam >= :mucMin)
        and
            (:mucMax is null or ctggm.mucGiam <= :mucMax)
    """)
    Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(
            @Param("tenChuongTrinh") String tenChuongTrinh,
            @Param("tenMon") String tenMon, // Đã map tham số tìm kiếm theo tên món ăn vào JPQL
            @Param("mucMin") BigDecimal mucMin,
            @Param("mucMax") BigDecimal mucMax,
            Pageable pageable
    );

    boolean existsByMon_IdMonAndDotGiamGia_IdDotGiamGia(
            Integer idMon,
            Integer idDotGiamGia
    );

    ChiTietGiamGiaMon findByMon_IdMonAndDotGiamGia_IdDotGiamGia(
            Integer idMon,
            Integer idDotGiamGia
    );
}