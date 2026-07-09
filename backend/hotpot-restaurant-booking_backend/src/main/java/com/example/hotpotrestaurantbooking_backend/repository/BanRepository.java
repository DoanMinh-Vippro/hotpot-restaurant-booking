package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BanRepository extends JpaRepository<Ban, Integer> {
    List<Ban> findByKhuVuc_IdKhuVuc(Integer idKhuVuc);
    List<Ban> findByTrangThai(TrangThaiBan trangThai);
}
