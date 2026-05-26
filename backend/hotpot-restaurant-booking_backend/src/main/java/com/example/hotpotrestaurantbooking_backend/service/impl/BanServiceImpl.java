// ===========================================
// BanServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.service.BanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BanServiceImpl implements BanService {

    @Autowired
    private BanRepository banRepository;

    @Override
    public List<Ban> getAll() {
        return banRepository.findAll();
    }

    @Override
    public Ban getById(Integer id) {
        return banRepository.findById(id).orElse(null);
    }

    @Override
    public Ban add(Ban ban) {
        return banRepository.save(ban);
    }

    @Override
    public Ban update(Integer id, Ban ban) {
        Ban old = getById(id);

        old.setLoaiBan(ban.getLoaiBan());
        old.setSoLuongBan(ban.getSoLuongBan());
        old.setKhuVuc(ban.getKhuVuc());
        old.setTrangThai(ban.getTrangThai());

        return banRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        banRepository.deleteById(id);
    }
}
