package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComboRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietCombo;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChiTietComBoValidator {
    private final ChiTietComboRepository repo;

    public void validateAdd(ChiTietComboRequest request) {

        String moTa = request.getMoTa();

        if (ValidateUtil.hasLeadingOrTrailingSpace(moTa)) {
            throw new RuntimeException("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(moTa)) {
            throw new RuntimeException("Mô tả không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (repo.existsByCombo_IdComboAndMon_IdMon(
                request.getIdCombo(),
                request.getIdMon())) {

            throw new RuntimeException("Món này đã tồn tại trong combo");
        }
    }

    public void validateUpdate(Integer idChiTietCombo,
                               ChiTietComboRequest request) {

        String moTa = request.getMoTa();

        if (ValidateUtil.hasLeadingOrTrailingSpace(moTa)) {
            throw new RuntimeException("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(moTa)) {
            throw new RuntimeException("Mô tả không được chứa nhiều khoảng trắng liên tiếp");
        }

        ChiTietCombo ctcbTrung = repo.findByCombo_IdComboAndMon_IdMon(
                request.getIdCombo(),
                request.getIdMon()
        );

        if (ctcbTrung != null
                && !ctcbTrung.getIdChiTietCombo().equals(idChiTietCombo)) {

            throw new RuntimeException("Món này đã tồn tại trong combo");
        }
    }
}
