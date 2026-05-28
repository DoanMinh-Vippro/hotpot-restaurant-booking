// ===========================================
// KhachHangServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    @Override
    public KhachHang getById(Integer id) {
        return khachHangRepository.findById(id).orElse(null);
    }

    @Override
    public KhachHang add(KhachHang khachHang) {
        return khachHangRepository.save(khachHang);
    }

    @Override
    public KhachHang update(Integer id, KhachHang khachHang) {

        KhachHang old = getById(id);

        old.setTenKhachHang(khachHang.getTenKhachHang());
        old.setSoDienThoai(khachHang.getSoDienThoai());
        old.setDiaChi(khachHang.getDiaChi());
        old.setEmail(khachHang.getEmail());

        return khachHangRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        khachHangRepository.deleteById(id);
    }
}