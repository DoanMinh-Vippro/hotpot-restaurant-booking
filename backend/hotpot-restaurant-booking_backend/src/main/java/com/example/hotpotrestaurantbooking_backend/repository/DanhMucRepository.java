package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer> {

        DanhMuc findByIdDanhMuc(Integer idDanhMuc);
        @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse(dm.idDanhMuc, dm.loaiDanhMuc, dm.moTa)
        from DanhMuc dm
                   
""")
    List<DanhMucResponse>hienThi();
        @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse(dm.idDanhMuc, dm.loaiDanhMuc, dm.moTa)
        from DanhMuc dm where dm.loaiDanhMuc=?1
""")
    DanhMucResponse deatilDM(String loaiDanhMuc);
    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse(dm.idDanhMuc, dm.loaiDanhMuc, dm.moTa)
        from DanhMuc dm
                   
""")
    Page<DanhMucResponse> phanTrangDanhMuc(Pageable pageable);
    @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse(dm.idDanhMuc, dm.loaiDanhMuc, dm.moTa)
        from DanhMuc dm 
        where (:loaiDanhMuc is null or lower(dm.loaiDanhMuc) like lower(concat('%', :loaiDanhMuc ,'%') ) )
                   
""")
    Page<DanhMucResponse> timKiemDanhMuc(
            @Param("loaiDanhMuc") String loaiDanhMuc,
            Pageable pageable
    );

    boolean existsByLoaiDanhMucIgnoreCase(String loaiDanhMuc);

    DanhMuc findByLoaiDanhMucIgnoreCase(String loaiDanhMuc);
}