package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo,Integer> {
    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ComboResponse(cb.idCombo, cb.tenCombo, cb.giaCombo, cb.hinhAnh, cb.trangThai)
        from Combo cb
""")
    List<ComboResponse>hienThiComBo();
    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ComboResponse(cb.idCombo, cb.tenCombo, cb.giaCombo, cb.hinhAnh, cb.trangThai)
        from Combo cb where cb.tenCombo=?1
""")
    ComboResponse detailComBo(String tenCombo);
    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.ComboResponse(cb.idCombo, cb.tenCombo, cb.giaCombo, cb.hinhAnh, cb.trangThai)
        from Combo cb
""")
    Page<ComboResponse> phanTrangComBo(Pageable pageable);

    @Query("""
select new com.example.hotpotrestaurantbooking_backend.dto.ComboResponse(cb.idCombo, cb.tenCombo, cb.giaCombo, cb.hinhAnh, cb.trangThai)
        from Combo cb
    where
        (:tenCombo is null or lower(cb.tenCombo) like lower(concat('%', :tenCombo, '%')))
    and
        (:giaMin is null or cb.giaCombo >= :giaMin)
    and
        (:giaMax is null or cb.giaCombo <= :giaMax)
""")
    Page<ComboResponse> timKiemComBo(
            @Param("tenCombo") String tenCombo,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            Pageable pageable
    );

    boolean existsByTenComboIgnoreCase(String tenCombo);

    Combo findByTenComboIgnoreCase(String tenCombo);

    Combo findByIdCombo(Integer idCombo);
}