package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.service.TinhTienService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TinhTienImpl implements TinhTienService {
    private final ChiTietGiamGiaMonRepository chiTietRepo;
    @Override
    public void ganThongTinGiamGia(MonResponse mon) {

        List<ChiTietGiamGiaMon> ds =
                 chiTietRepo.findByMon_IdMon(mon.getIdMon());

        if (ds.isEmpty()) {
            return;
        }

        ChiTietGiamGiaMon ct = ds.get(0);

        BigDecimal tienGiam;

        if ("PHANTRAM".equalsIgnoreCase(ct.getLoaiGiam())) {
            tienGiam = mon.getDonGiaHienTai()
                    .multiply(ct.getMucGiam())
                    .divide(BigDecimal.valueOf(100));
        } else {
            tienGiam = ct.getMucGiam();
        }

        mon.setSoTienDuocGiam(tienGiam);
        mon.setGiaSauGiam(
                mon.getDonGiaHienTai().subtract(tienGiam)
        );

        mon.setTenChuongTrinhGiamGia(
                ct.getDotGiamGia().getTenChuongTrinh()
        );
    }
}
