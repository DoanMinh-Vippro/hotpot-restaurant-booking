package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.DanhMucDTO;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer> {

        DanhMuc findByIdDanhMuc(Integer idDanhMuc);
        @Query("""
        select new com.example.hotpotrestaurantbooking_backend.dto.DanhMucDTO(dm.idDanhMuc, dm.loaiDanhMuc, dm.loaiDanhMuc)
        from DanhMuc dm
                   
""")
    List<DanhMucDTO>hienThi();

}