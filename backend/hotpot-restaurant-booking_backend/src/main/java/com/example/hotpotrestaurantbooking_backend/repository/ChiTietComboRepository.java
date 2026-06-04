package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietCombo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ChiTietComboRepository extends JpaRepository<ChiTietCombo,Integer> {
    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.idMon,ctcb.mon.tenMon, ctcb.combo.idCombo,ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon 
""")
    List<ChiTietComBoResponse>hienThi();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.idMon,ctcb.mon.tenMon, ctcb.combo.idCombo,ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon where ctcb.idChiTietCombo=?1
""")
    ChiTietComBoResponse detailChiTietComBo(Integer idChiTietCombo);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.idMon,ctcb.mon.tenMon, ctcb.combo.idCombo,ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon 
""")
    Page<ChiTietComBoResponse> phanTrangCTComBo(Pageable pageable);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
        ctcb.idChiTietCombo, 
        ctcb.soLuong, 
        m.idMon, 
        m.tenMon, 
        cb.idCombo, 
        cb.tenCombo, 
        cb.giaCombo, 
        cb.hinhAnh, 
        cb.trangThai, 
        ctcb.moTa
    )
    from ChiTietCombo ctcb 
    join ctcb.combo cb 
    join ctcb.mon m 
    where
        (:tenCombo is null or :tenCombo = '' or lower(cb.tenCombo) like lower(concat('%', :tenCombo, '%')))
    and
        (:tenMon is null or :tenMon = '' or lower(m.tenMon) like lower(concat('%', :tenMon, '%')))
    and
        (:giaMin is null or cb.giaCombo >= :giaMin)
    and
        (:giaMax is null or cb.giaCombo <= :giaMax)
""")
    Page<ChiTietComBoResponse> timKiemCTCB(
            @Param("tenCombo") String tenCombo,
            @Param("tenMon") String tenMon,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            Pageable pageable
    );

    @Query("select ctcb.mon.tenMon from ChiTietCombo ctcb where ctcb.combo.idCombo = :idCombo")
    List<String> findMonNamesByComboId(@Param("idCombo") Integer idCombo);

    boolean existsByCombo_IdComboAndMon_IdMon(
            Integer idCombo,
            Integer idMon
    );

    ChiTietCombo findByCombo_IdComboAndMon_IdMon(
            Integer idCombo,
            Integer idMon
    );
}