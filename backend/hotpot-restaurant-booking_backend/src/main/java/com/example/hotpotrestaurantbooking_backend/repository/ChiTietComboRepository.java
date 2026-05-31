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
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.tenMon, ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon 
""")
    List<ChiTietComBoResponse>hienThi();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.tenMon, ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon where ctcb.idChiTietCombo=?1
""")
    ChiTietComBoResponse detailChiTietComBo(Integer idChiTietCombo);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.tenMon, ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon 
""")
    Page<ChiTietComBoResponse> phanTrangCTComBo(Pageable pageable);

    @Query("""
select new com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse(
    ctcb.idChiTietCombo, ctcb.soLuong, ctcb.mon.tenMon, ctcb.combo.tenCombo, ctcb.combo.giaCombo, ctcb.combo.hinhAnh, ctcb.combo.trangThai, ctcb.moTa
    )
    from ChiTietCombo ctcb join Combo cb on ctcb.combo.idCombo=cb.idCombo 
    join Mon m on ctcb.mon.idMon=m.idMon 
    where
        (:tenCombo is null or lower(ctcb.combo.tenCombo) like lower(concat('%', :tenCombo, '%')))
    and
        (:tenMon is null or lower(ctcb.mon.tenMon) like lower(concat('%', :tenMon, '%')))
    and
        (:giaMin is null or ctcb.combo.giaCombo >= :giaMin)
    and
        (:giaMax is null or ctcb.combo.giaCombo <= :giaMax)
""")
    Page<ChiTietComBoResponse> timKiemCTCB(
            @Param("tenCombo") String tenCombo,
            @Param("tenMon") String tenMon,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            Pageable pageable
    );
    boolean existsByCombo_IdComboAndMon_IdMon(
            Integer idCombo,
            Integer idMon
    );

    ChiTietCombo findByCombo_IdComboAndMon_IdMon(
            Integer idCombo,
            Integer idMon
    );
}