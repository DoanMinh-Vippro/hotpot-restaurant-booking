package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon,Integer> {
    Optional<HoaDon> findByBan_IdBanAndTrangThaiHoaDon(Integer idBan, Integer trangThaiHoaDon);
    boolean existsByDatBan_IdDatBanAndTrangThaiThanhToan(Integer idDatBan, Integer trangThaiThanhToan);
}