package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiamGiaRepository extends JpaRepository<GiamGia,Integer> {
    Page<GiamGia> findByMaGiamGiaContainingIgnoreCaseOrDieuKienSuDungContainingIgnoreCase(
            String maGiamGia, String dieuKienSuDung, Pageable pageable);

    boolean existsByMaGiamGia(String maGiamGia);
}
