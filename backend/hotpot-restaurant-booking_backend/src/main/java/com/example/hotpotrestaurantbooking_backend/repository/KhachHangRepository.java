package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    List<KhachHang>
    findByTenKhachHangContainingOrMaKhachHangContainingOrSoDienThoaiContainingOrEmailContainingOrTaiKhoan_MaTaiKhoanContaining(
            String tenKhachHang,
            String maKhachHang,
            String soDienThoai,
            String email,
            String maTaiKhoan
    );

    boolean existsBySoDienThoai(String soDienThoai);
    
    KhachHang findByTaiKhoan(TaiKhoan taiKhoan);

    Optional<KhachHang> findByTaiKhoan_IdTaiKhoan(Integer idTaiKhoan);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
}
