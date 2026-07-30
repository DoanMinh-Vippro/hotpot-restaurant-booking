package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet,Integer> {
    List<HoaDonChiTiet> findByHoaDon_IdHoaDon(Integer idHoaDon);
    void deleteByHoaDon_IdHoaDon(Integer hoaDonIdHoaDon);
    Optional<HoaDonChiTiet> findByHoaDon_IdHoaDonAndMon_IdMon(Integer idHoaDon, Integer idMon);
    Optional<HoaDonChiTiet> findByHoaDon_IdHoaDonAndCombo_IdCombo(Integer idHoaDon, Integer idCombo);
}
