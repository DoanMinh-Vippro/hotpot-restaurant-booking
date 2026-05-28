// ===========================================
// MonServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.MonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonServiceImpl implements MonService {

    @Autowired
    private MonRepository monRepository;

    @Override
    public List<Mon> getAll() {
        return monRepository.findAll();
    }

    @Override
    public Mon getById(Integer id) {
        return monRepository.findById(id).orElse(null);
    }

    @Override
    public Mon add(Mon mon) {
        return monRepository.save(mon);
    }

    @Override
    public Mon update(Integer id, Mon mon) {

        Mon old = getById(id);

        old.setTenMon(mon.getTenMon());
        old.setDonGiaHienTai(mon.getDonGiaHienTai());
        old.setDanhMuc(mon.getDanhMuc());

        return monRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        monRepository.deleteById(id);
    }
}