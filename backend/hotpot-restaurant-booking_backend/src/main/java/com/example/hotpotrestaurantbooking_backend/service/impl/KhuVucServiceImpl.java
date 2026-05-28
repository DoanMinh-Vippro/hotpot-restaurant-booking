// ===========================================
// KhuVucServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.KhuVuc;
import com.example.hotpotrestaurantbooking_backend.repository.KhuVucRepository;
import com.example.hotpotrestaurantbooking_backend.service.KhuVucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhuVucServiceImpl implements KhuVucService {

    @Autowired
    private KhuVucRepository repository;

    @Override
    public List<KhuVuc> getAll() {
        return repository.findAll();
    }

    @Override
    public KhuVuc getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public KhuVuc add(KhuVuc khuVuc) {
        return repository.save(khuVuc);
    }

    @Override
    public KhuVuc update(Integer id, KhuVuc khuVuc) {

        KhuVuc old = getById(id);

        old.setMoTa(khuVuc.getMoTa());
        old.setTenKhuVuc(khuVuc.getTenKhuVuc());
        old.setTrangThai(khuVuc.getTrangThai());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}