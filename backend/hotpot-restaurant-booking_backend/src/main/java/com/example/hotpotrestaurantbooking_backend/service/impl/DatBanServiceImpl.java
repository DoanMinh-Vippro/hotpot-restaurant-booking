// ===========================================
// DatBanServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatBanServiceImpl implements DatBanService {

    @Autowired
    private DatBanRepository datBanRepository;

    @Override
    public List<DatBan> getAll() {
        return datBanRepository.findAll();
    }

    @Override
    public DatBan getById(Integer id) {
        return datBanRepository.findById(id).orElse(null);
    }

    @Override
    public DatBan add(DatBan datBan) {
        return datBanRepository.save(datBan);
    }

    @Override
    public DatBan update(Integer id, DatBan datBan) {

        DatBan old = getById(id);

        old.setBan(datBan.getBan());
        old.setKhachHang(datBan.getKhachHang());
        old.setNgayDat(datBan.getNgayDat());
        old.setGioDat(datBan.getGioDat());
        old.setSdtKhachHang(datBan.getSdtKhachHang());
        old.setSoNguoi(datBan.getSoNguoi());
        old.setTrangThai(datBan.getTrangThai());
        old.setGhiChu(datBan.getGhiChu());
        old.setThoiGianDenDuKien(datBan.getThoiGianDenDuKien());
        old.setSoTienCoc(datBan.getSoTienCoc());
        old.setTrangThaiCoc(datBan.getTrangThaiCoc());
        old.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan());

        return datBanRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }
}