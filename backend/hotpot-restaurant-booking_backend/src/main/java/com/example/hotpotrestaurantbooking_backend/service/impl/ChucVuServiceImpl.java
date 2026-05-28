// ===========================================
// ChucVuServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChucVuServiceImpl implements ChucVuService {

    @Autowired
    private ChucVuRepository repository;

    @Override
    public List<ChucVu> getAll() {
        return repository.findAll();
    }

    @Override
    public ChucVu getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ChucVu add(ChucVu chucVu) {
        return repository.save(chucVu);
    }

    @Override
    public ChucVu update(Integer id, ChucVu chucVu) {

        ChucVu old = getById(id);

        old.setMaChucVu(chucVu.getMaChucVu());
        old.setTenChucVu(chucVu.getTenChucVu());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}