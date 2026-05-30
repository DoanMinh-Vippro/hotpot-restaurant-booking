package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}