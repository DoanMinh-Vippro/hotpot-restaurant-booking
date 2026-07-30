package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaComBo;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ChiTietGiamGiaComBoValidator {
    private final ChiTietGiamGiaComBoRepository chiTietRepository;
    private final ComboRepository comboRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;

    // ====== ADD ======
    public void validateAdd(ChiTietGiamGiaComboRequest request) {

        Integer idCombo = request.getIdCombo();
        Integer idDotGiamGia = request.getIdDotGiamGia();
        var mucGiam = request.getMucGiam();

        // ====== check tồn tại FK ======
        if (!comboRepository.existsById(idCombo)) {
            throw new RuntimeException("Combo không tồn tại");
        }

        if (!dotGiamGiaRepository.existsById(idDotGiamGia)) {
            throw new RuntimeException("Đợt giảm giá không tồn tại");
        }

        // ====== check duplicate (1 combo không được trùng trong 1 đợt giảm giá) ======
        if (chiTietRepository.existsByCombo_IdComboAndDotGiamGia_IdDotGiamGia(idCombo, idDotGiamGia)) {
            throw new RuntimeException("Combo này đã được áp dụng trong đợt giảm giá này");
        }

        // ====== check mức giảm ======
        if (mucGiam == null) {
            throw new RuntimeException("Mức giảm không được để trống");
        }

        if (mucGiam.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mức giảm phải lớn hơn 0");
        }

        // ====== check loại giảm ======
        if (ValidateUtil.isBlank(request.getLoaiGiam())) {
            throw new RuntimeException("Loại giảm không được để trống");
        }

        String loaiGiam = request.getLoaiGiam().trim().toUpperCase();

        if (!loaiGiam.equals("TIEN") && !loaiGiam.equals("PHANTRAM")) {
            throw new RuntimeException("Loại giảm chỉ được là TIEN hoặc PHANTRAM");
        }

        // ====== check giá combo ======
        Combo combo = comboRepository.findById(idCombo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy combo"));

        if (loaiGiam.equals("PHANTRAM") && mucGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Phần trăm giảm không được vượt quá 100%");
        }

        if (loaiGiam.equals("TIEN") && mucGiam.compareTo(combo.getGiaCombo()) > 0) {
            throw new RuntimeException("Số tiền giảm không được lớn hơn giá combo");
        }
    }

    // ====== UPDATE ======
    public void validateUpdate(Integer idChiTiet, ChiTietGiamGiaComboRequest request) {

        Integer idCombo = request.getIdCombo();
        Integer idDotGiamGia = request.getIdDotGiamGia();
        var mucGiam = request.getMucGiam();

        ChiTietGiamGiaComBo exist = chiTietRepository.findByCombo_IdComboAndDotGiamGia_IdDotGiamGia(idCombo, idDotGiamGia);

        if (exist != null && !exist.getIdChiTietGiamGiaCombo().equals(idChiTiet)) {
            throw new RuntimeException("Combo này đã được áp dụng trong đợt giảm giá này");
        }

        if (mucGiam == null) {
            throw new RuntimeException("Mức giảm không được để trống");
        }

        if (mucGiam.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mức giảm phải lớn hơn 0");
        }

        if (ValidateUtil.isBlank(request.getLoaiGiam())) {
            throw new RuntimeException("Loại giảm không được để trống");
        }

        String loaiGiam = request.getLoaiGiam().trim().toUpperCase();

        if (!loaiGiam.equals("TIEN") && !loaiGiam.equals("PHANTRAM")) {
            throw new RuntimeException("Loại giảm chỉ được là TIEN hoặc PHANTRAM");
        }

        Combo combo = comboRepository.findById(idCombo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy combo"));

        if (loaiGiam.equals("PHANTRAM") && mucGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Phần trăm giảm không được vượt quá 100%");
        }

        if (loaiGiam.equals("TIEN") && mucGiam.compareTo(combo.getGiaCombo()) > 0) {
            throw new RuntimeException("Số tiền giảm không được lớn hơn giá combo");
        }
    }
}
