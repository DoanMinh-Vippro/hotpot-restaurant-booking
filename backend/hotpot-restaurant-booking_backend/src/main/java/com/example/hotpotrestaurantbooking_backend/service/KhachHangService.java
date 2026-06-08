
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;

import java.util.List;

public interface KhachHangService {


    List<KhachHang> getAll();

    KhachHang getById(Integer id);

    KhachHang add(KhachHang khachHang);

    KhachHang update(Integer id, KhachHang khachHang);

    void delete(Integer id);

    
}

