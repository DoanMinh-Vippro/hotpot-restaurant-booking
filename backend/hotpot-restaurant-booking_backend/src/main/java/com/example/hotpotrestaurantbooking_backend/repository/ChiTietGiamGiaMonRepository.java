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
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon
""")
    List<ChiTietGiamGiaMonResponse> hienThiCTGGM();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon 
    where ctggm.idChiTietGiamGiaMon=?1
""")
    ChiTietGiamGiaMonResponse detailCTGGM(Integer idChiTietGiamGiaMon);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon
""")
    Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(Pageable pageable);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse(
    ctggm.idChiTietGiamGiaMon, ctggm.dotGiamGia.tenChuongTrinh,
    ctggm.mon.tenMon, ctggm.mucGiam
    )
    from ChiTietGiamGiaMon ctggm 
    join DotGiamGia dgg on ctggm.dotGiamGia.idDotGiamGia=dgg.idDotGiamGia
    join Mon m on ctggm.mon.idMon=m.idMon
    where 
        (:tenChuongTrinh is null or lower(ctggm.dotGiamGia.tenChuongTrinh) like lower(concat('%', :tenChuongTrinh , '%')))
    AND 
        (:mucMin is null or ctggm.mucGiam >= :mucMin)
    AND 
        (:mucMax is null or ctggm.mucGiam <= :mucMax)
""")
    Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(
            @Param("tenChuongTrinh") String tenChuongTrinh,
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