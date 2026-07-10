package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.ChiTietDatBanCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChiTietDatBanComboRepository extends JpaRepository<ChiTietDatBanCombo, Integer> {

    List<ChiTietDatBanCombo> findByDatBan_IdDatBan(Integer idDatBan);

    @Modifying
    @Transactional
    void deleteByDatBan_IdDatBan(Integer idDatBan);
}