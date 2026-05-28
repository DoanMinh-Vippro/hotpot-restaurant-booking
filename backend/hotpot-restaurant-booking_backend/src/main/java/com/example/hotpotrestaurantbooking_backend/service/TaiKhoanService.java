// ===========================================
// TaiKhoanService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;

import java.util.List;

public interface TaiKhoanService {

    List<TaiKhoan> getAll();

    TaiKhoan getById(Integer id);

    TaiKhoan add(TaiKhoan taiKhoan);

    TaiKhoan update(Integer id, TaiKhoan taiKhoan);

    void delete(Integer id);
}