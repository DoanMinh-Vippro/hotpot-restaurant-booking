package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.ChiTietDatBanBan;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietDatBanBanRepository extends JpaRepository<ChiTietDatBanBan, Integer> {

    List<ChiTietDatBanBan> findByDatBan_IdDatBan(Integer idDatBan);

    void deleteByDatBan_IdDatBan(Integer idDatBan);

    void deleteAllByDatBan(DatBan datBan);


}