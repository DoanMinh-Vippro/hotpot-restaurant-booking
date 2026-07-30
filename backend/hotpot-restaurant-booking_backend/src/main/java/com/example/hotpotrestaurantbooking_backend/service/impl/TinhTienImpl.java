package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaComBo;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaComBoRepository;
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
    private final ChiTietGiamGiaComBoRepository repo2;
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

    @Override
    public void ganThongTinGiamGiaCombo(ComboResponse combo) {
        LocalDate homNay = LocalDate.now();

        List<ChiTietGiamGiaComBo> ds = repo2.findByCombo_IdCombo(combo.getIdCombo());

        if (ds == null || ds.isEmpty()) {
            combo.setGiaSauGiam(combo.getGiaCombo());
            combo.setSoTienDuocGiam(BigDecimal.ZERO);
            combo.setTenChuongTrinhGiamGia("Không trong chương trình giảm giá");
            return;
        }

        ChiTietGiamGiaComBo ct = ds.stream()
                .filter(x -> x.getTrangThai() != null && x.getTrangThai() == 0
                        && x.getDotGiamGia() != null
                        && x.getDotGiamGia().getNgayKetThuc() != null
                        && !x.getDotGiamGia().getNgayKetThuc().isBefore(homNay))
                .findFirst()
                .orElse(null);

        if (ct == null) {
            combo.setGiaSauGiam(combo.getGiaCombo());
            combo.setSoTienDuocGiam(BigDecimal.ZERO);
            combo.setTenChuongTrinhGiamGia("Không trong chương trình giảm giá");
            return;
        }

        BigDecimal tienGiam;
        if ("PHANTRAM".equalsIgnoreCase(ct.getLoaiGiam())) {
            tienGiam = combo.getGiaCombo()
                    .multiply(ct.getMucGiam())
                    .divide(BigDecimal.valueOf(100));
        } else {
            tienGiam = ct.getMucGiam();
        }

        combo.setSoTienDuocGiam(tienGiam);
        combo.setGiaSauGiam(combo.getGiaCombo().subtract(tienGiam));
        combo.setTenChuongTrinhGiamGia(ct.getDotGiamGia().getTenChuongTrinh());
    }
}
