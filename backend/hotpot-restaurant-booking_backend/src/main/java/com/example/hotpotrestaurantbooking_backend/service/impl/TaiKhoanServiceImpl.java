// ===========================================
// TaiKhoanServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaiKhoanServiceImpl implements TaiKhoanService {

    @Autowired
    private TaiKhoanRepository repository;

    @Override
    public List<TaiKhoan> getAll() {
        return repository.findAll();
    }

    @Override
    public TaiKhoan getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public TaiKhoan add(TaiKhoan taiKhoan) {
        return repository.save(taiKhoan);
    }

    @Override
    public TaiKhoan update(Integer id, TaiKhoan taiKhoan) {

        TaiKhoan old = getById(id);

        old.setMaTaiKhoan(taiKhoan.getMaTaiKhoan());
        old.setTenDangNhap(taiKhoan.getTenDangNhap());
        old.setMatKhau(taiKhoan.getMatKhau());
        old.setTrangThai(taiKhoan.getTrangThai());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
