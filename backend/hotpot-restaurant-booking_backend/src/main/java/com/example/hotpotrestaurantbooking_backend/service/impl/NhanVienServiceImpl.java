// ===========================================
// NhanVienServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import com.example.hotpotrestaurantbooking_backend.repository.NhanVienRepository;
import com.example.hotpotrestaurantbooking_backend.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository repository;

    @Override
    public List<NhanVien> getAll() {
        return repository.findAll();
    }

    @Override
    public NhanVien getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public NhanVien add(NhanVien nhanVien) {
        return repository.save(nhanVien);
    }

    @Override
    public NhanVien update(Integer id, NhanVien nhanVien) {

        NhanVien old = getById(id);

        old.setMaNhanVien(nhanVien.getMaNhanVien());
        old.setTenNhanVien(nhanVien.getTenNhanVien());
        old.setGioiTinh(nhanVien.getGioiTinh());
        old.setSoDienThoai(nhanVien.getSoDienThoai());
        old.setEmail(nhanVien.getEmail());
        old.setChucVu(nhanVien.getChucVu());
        old.setTaiKhoan(nhanVien.getTaiKhoan());
        old.setDiaChi(nhanVien.getDiaChi());
        old.setTrangThai(nhanVien.getTrangThai());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}