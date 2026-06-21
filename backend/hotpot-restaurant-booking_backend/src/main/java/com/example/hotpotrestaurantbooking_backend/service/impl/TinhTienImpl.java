package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.service.TinhTienService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TinhTienImpl implements TinhTienService {
    private final ChiTietGiamGiaMonRepository chiTietRepo;
    @Override
    public void ganThongTinGiamGia(MonResponse mon) {
        LocalDate homNay = LocalDate.now();

        List<ChiTietGiamGiaMon> ds =
                 chiTietRepo.findByMon_IdMon(mon.getIdMon());

        if (ds.isEmpty()) {
            mon.setGiaSauGiam(mon.getDonGiaHienTai());
            mon.setSoTienDuocGiam(BigDecimal.ZERO);
            mon.setTenChuongTrinhGiamGia("Không trong chương trình giảm giá");
            return;
        }

        ChiTietGiamGiaMon ct = ds.stream()
                .filter(x ->
                        x.getTrangThai() == 0
                                && !x.getDotGiamGia()
                                .getNgayKetThuc()
                                .isBefore(homNay)
                )
                .findFirst()
                .orElse(null);

        if (ct == null) {
            mon.setGiaSauGiam(mon.getDonGiaHienTai());
            mon.setSoTienDuocGiam(BigDecimal.ZERO);
            mon.setTenChuongTrinhGiamGia("Không trong chương trình giảm giá");
            return;
        }

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
