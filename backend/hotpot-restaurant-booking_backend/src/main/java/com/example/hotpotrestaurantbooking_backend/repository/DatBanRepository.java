package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DatBanRepository extends JpaRepository<DatBan,Integer> {
    List<DatBan> findByKhachHang_IdKhachHang(Integer id);
}
