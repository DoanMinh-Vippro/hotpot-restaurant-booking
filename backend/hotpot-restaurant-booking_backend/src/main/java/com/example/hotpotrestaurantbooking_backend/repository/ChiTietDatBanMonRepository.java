package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.ChiTietDatBanMon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDatBanMonRepository extends JpaRepository<ChiTietDatBanMon, Integer> {
    List<ChiTietDatBanMon> findByDatBan_IdDatBan(Integer idDatBan);
    void deleteByDatBan_IdDatBan(Integer idDatBan);
}