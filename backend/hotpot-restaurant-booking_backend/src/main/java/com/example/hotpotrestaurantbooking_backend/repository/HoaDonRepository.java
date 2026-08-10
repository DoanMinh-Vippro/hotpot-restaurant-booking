package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon,Integer> {
    Optional<HoaDon> findByBan_IdBanAndTrangThaiHoaDon(Integer idBan, Integer trangThaiHoaDon);
    Optional<HoaDon> findFirstByBan_IdBanAndTrangThaiHoaDonAndTrangThaiThanhToan(Integer idBan, Integer trangThaiHoaDon, Integer trangThaiThanhToan);
    boolean existsByDatBan_IdDatBanAndTrangThaiThanhToan(Integer idDatBan, Integer trangThaiThanhToan);
    Optional<HoaDon> findByDatBan_IdDatBan(Integer idDatBan);
    List<HoaDon> findByTrangThaiHoaDon(Integer trangThaiHoaDon);
    List<HoaDon> findByIdHoaDonIn(List<Integer> ids);
    List<HoaDon> findByTrangThaiHoaDonAndTrangThaiThanhToanAndBanIsNotNull(Integer trangThaiHoaDon, Integer trangThaiThanhToan);
    long countByTrangThaiHoaDonAndTrangThaiThanhToanAndBanIsNotNull(Integer trangThaiHoaDon, Integer trangThaiThanhToan);
    long countByTrangThaiThanhToan(Integer trangThaiThanhToan);
}