package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia,Integer> {
    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse(dgg.idDotGiamGia, dgg.tenChuongTrinh, dgg.ngayBatDau, dgg.ngayKetThuc)
    from DotGiamGia dgg
""")
    List<DotGiamGiaResponse>hienThiDGG();

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse(dgg.idDotGiamGia, dgg.tenChuongTrinh, dgg.ngayBatDau, dgg.ngayKetThuc)
    from DotGiamGia dgg where dgg.tenChuongTrinh=?1
""")
    DotGiamGiaResponse detailDGG(String tenChuongTrinh);

    @Query("""
    select new com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse(dgg.idDotGiamGia, dgg.tenChuongTrinh, dgg.ngayBatDau, dgg.ngayKetThuc)
    from DotGiamGia dgg
""")
    Page<DotGiamGiaResponse> phanTrangDGG(Pageable pageable);

    @Query("""
select new com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse(dgg.idDotGiamGia, dgg.tenChuongTrinh, dgg.ngayBatDau, dgg.ngayKetThuc)
    from DotGiamGia dgg
    where
        (:tenChuongTrinh is null or lower(dgg.tenChuongTrinh) like lower(concat('%', :tenChuongTrinh, '%')))
    and
        (:tuNgay is null or dgg.ngayBatDau >= :tuNgay)
    and
        (:denNgay is null or dgg.ngayKetThuc <= :denNgay)
""")
    Page<DotGiamGiaResponse> timKiemDGG(
            @Param("tenChuongTrinh") String tenChuongTrinh,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable
    );

    DotGiamGia findByIdDotGiamGia(Integer idDotGiamGia);
}
