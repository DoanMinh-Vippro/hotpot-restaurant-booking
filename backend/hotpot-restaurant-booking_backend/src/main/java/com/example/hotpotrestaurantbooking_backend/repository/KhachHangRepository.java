package com.example.hotpotrestaurantbooking_backend.repository;

import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    List<KhachHang>
    findByTenKhachHangContainingOrMaKhachHangContainingOrSoDienThoaiContainingOrEmailContainingOrTaiKhoan_MaTaiKhoanContaining(
            String tenKhachHang,
            String maKhachHang,
            String soDienThoai,
            String email,
            String maTaiKhoan
    );
}
