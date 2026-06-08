package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KhuVucRepository extends JpaRepository<KhuVuc,Integer> {
    // Tìm theo tên khu vực
    Optional<KhuVuc> findByTenKhuVuc(String tenKhuVuc);

    // Tìm theo trạng thái
    List<KhuVuc> findByTrangThaiOrderByIdKhuVuc(Integer trangThai);
    // Kiểm tra tồn tại theo tên
    boolean existsByTenKhuVuc(String tenKhuVuc);
    @Query("SELECT k FROM KhuVuc k LEFT JOIN FETCH k.banList")
    List<KhuVuc> findAllKhuVucSạch();;
}
