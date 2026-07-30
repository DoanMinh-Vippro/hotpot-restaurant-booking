package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaComBo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
@Repository
public interface ChiTietGiamGiaComBoRepository extends JpaRepository<ChiTietGiamGiaComBo, Integer> {

    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse(
            ctggcb.idChiTietGiamGiaCombo, dgg.idDotGiamGia, dgg.tenChuongTrinh,
            c.idCombo, c.tenCombo, ctggcb.mucGiam, ctggcb.loaiGiam, ctggcb.trangThai
        )
        from ChiTietGiamGiaComBo ctggcb
        join ctggcb.dotGiamGia dgg
        join ctggcb.combo c
    """)
    List<ChiTietGiamGiaComboResponse> hienThiCTGGCombo();

    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse(
            ctggcb.idChiTietGiamGiaCombo, dgg.idDotGiamGia, dgg.tenChuongTrinh,
            c.idCombo, c.tenCombo, ctggcb.mucGiam, ctggcb.loaiGiam, ctggcb.trangThai
        )
        from ChiTietGiamGiaComBo ctggcb
        join ctggcb.dotGiamGia dgg
        join ctggcb.combo c
        where ctggcb.idChiTietGiamGiaCombo = :idChiTietGiamGiaCombo
    """)
    ChiTietGiamGiaComboResponse detailCTGGCombo(@Param("idChiTietGiamGiaCombo") Integer idChiTietGiamGiaCombo);

    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse(
            ctggcb.idChiTietGiamGiaCombo, dgg.idDotGiamGia, dgg.tenChuongTrinh,
            c.idCombo, c.tenCombo, ctggcb.mucGiam, ctggcb.loaiGiam, ctggcb.trangThai
        )
        from ChiTietGiamGiaComBo ctggcb
        join ctggcb.dotGiamGia dgg
        join ctggcb.combo c
    """)
    Page<ChiTietGiamGiaComboResponse> phanTrangCTGGCombo(Pageable pageable);

    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse(
            ctggcb.idChiTietGiamGiaCombo, dgg.idDotGiamGia, dgg.tenChuongTrinh,
            c.idCombo, c.tenCombo, ctggcb.mucGiam, ctggcb.loaiGiam, ctggcb.trangThai
        )
        from ChiTietGiamGiaComBo ctggcb
        join ctggcb.dotGiamGia dgg
        join ctggcb.combo c
        where
            (:tenChuongTrinh is null or dgg.tenChuongTrinh like :tenChuongTrinh)
        and
            (:tenCombo is null or c.tenCombo like :tenCombo)
        and
            (:mucMin is null or ctggcb.mucGiam >= :mucMin)
        and
            (:mucMax is null or ctggcb.mucGiam <= :mucMax)
        and
            (:loaiGiam is null or ctggcb.loaiGiam = :loaiGiam)
    """)
    Page<ChiTietGiamGiaComboResponse> timKiemCTGGCombo(
            @Param("tenChuongTrinh") String tenChuongTrinh,
            @Param("tenCombo") String tenCombo,
            @Param("mucMin") BigDecimal mucMin,
            @Param("mucMax") BigDecimal mucMax,
            @Param("loaiGiam") String loaiGiam,
            Pageable pageable
    );

    boolean existsByCombo_IdComboAndDotGiamGia_IdDotGiamGia(
            Integer idCombo,
            Integer idDotGiamGia
    );

    ChiTietGiamGiaComBo findByCombo_IdComboAndDotGiamGia_IdDotGiamGia(
            Integer idCombo,
            Integer idDotGiamGia
    );

    List<ChiTietGiamGiaComBo> findByCombo_IdCombo(Integer idCombo);
    List<ChiTietGiamGiaComBo> findAllByTrangThai(Integer trangThai);
}
