// ===========================================
// NhanVienService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;

import java.util.List;

public interface NhanVienService {

    List<NhanVien> getAll();

    NhanVien getById(Integer id);

    NhanVien add(NhanVien nhanVien);

    NhanVien update(Integer id, NhanVien nhanVien);

    void delete(Integer id);
}