package com.example.hotpotrestaurantbooking_backend.repository;


import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    NhanVien findByTaiKhoan_IdTaiKhoan(Integer idTaiKhoan);
}
