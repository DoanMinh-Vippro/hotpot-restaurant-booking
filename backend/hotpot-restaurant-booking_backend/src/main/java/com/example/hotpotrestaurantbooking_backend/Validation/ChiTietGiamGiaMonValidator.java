package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChiTietGiamGiaMonValidator {
    private final ChiTietGiamGiaMonRepository chiTietRepository;
    private final MonRepository monRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;

    // ====== ADD ======
    public void validateAdd(ChiTietGiamGiaMonRequest request) {

        Integer idMon = request.getIdMon();
        Integer idDotGiamGia = request.getIdDotGiamGia();
        var mucGiam = request.getMucGiam();

        // ====== check tồn tại FK ======
        if (!monRepository.existsById(idMon)) {
            throw new RuntimeException("Món không tồn tại");
        }

        if (!dotGiamGiaRepository.existsById(idDotGiamGia)) {
            throw new RuntimeException("Đợt giảm giá không tồn tại");
        }

        // ====== check duplicate (1 món không được trùng trong 1 đợt giảm giá) ======
        if (chiTietRepository.existsByMon_IdMonAndDotGiamGia_IdDotGiamGia(
                idMon,
                idDotGiamGia
        )) {
            throw new RuntimeException("Món này đã được áp dụng trong đợt giảm giá này");
        }

        // ====== check mức giảm ======
        if (mucGiam == null) {
            throw new RuntimeException("Mức giảm không được để trống");
        }

        if (mucGiam.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mức giảm phải lớn hơn 0");
        }
    }

    // ====== UPDATE ======
    public void validateUpdate(Integer idChiTiet, ChiTietGiamGiaMonRequest request) {

        Integer idMon = request.getIdMon();
        Integer idDotGiamGia = request.getIdDotGiamGia();
        var mucGiam = request.getMucGiam();

        ChiTietGiamGiaMon exist =
                chiTietRepository.findByMon_IdMonAndDotGiamGia_IdDotGiamGia(
                        idMon,
                        idDotGiamGia
                );

        if (exist != null && !exist.getIdChiTietGiamGiaMon().equals(idChiTiet)) {
            throw new RuntimeException("Món này đã được áp dụng trong đợt giảm giá này");
        }

        if (mucGiam == null) {
            throw new RuntimeException("Mức giảm không được để trống");
        }

        if (mucGiam.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mức giảm phải lớn hơn 0");
        }
    }
}
