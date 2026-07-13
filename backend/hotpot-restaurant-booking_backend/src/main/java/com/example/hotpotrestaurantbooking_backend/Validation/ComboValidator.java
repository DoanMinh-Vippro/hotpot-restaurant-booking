package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ComboRequest;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ComboValidator {
    private final ComboRepository comboRepository;

    public void validateCreate(ComboRequest request){

        String tenCombo = request.getTenCombo();

        if (ValidateUtil.hasLeadingOrTrailingSpace(tenCombo)) {
            throw new RuntimeException("Tên combo không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(tenCombo)) {
            throw new RuntimeException("Tên combo không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (comboRepository.existsByTenComboIgnoreCase(tenCombo.trim())) {
            throw new RuntimeException("Tên combo đã tồn tại");
        }

        if (request.getTrangThai() == 0 && request.getTrangThaiBan() == 1) {
            throw new RuntimeException("Combo đang ngừng bán, trạng thái bán phải là 'Hết hàng'");
        }
    }

    public void validateUpdate(Integer idCombo, ComboRequest request) {

        String tenCombo = request.getTenCombo();

        Combo comboTrungTen =
                comboRepository.findByTenComboIgnoreCase(tenCombo.trim());

        if (comboTrungTen != null
                && !comboTrungTen.getIdCombo().equals(idCombo)) {

            throw new RuntimeException("Tên combo đã tồn tại");
        }

        if (request.getTrangThai() == 0 && request.getTrangThaiBan() == 1) {
            throw new RuntimeException("Combo đang ngừng bán, trạng thái bán phải là 'Hết hàng'");
        }

        if (ValidateUtil.hasLeadingOrTrailingSpace(tenCombo)) {
            throw new RuntimeException("Tên combo không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(tenCombo)) {
            throw new RuntimeException("Tên combo không được chứa nhiều khoảng trắng liên tiếp");
        }
    }
}
