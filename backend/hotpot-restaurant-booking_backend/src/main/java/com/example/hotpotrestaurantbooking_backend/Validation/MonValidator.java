package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.MonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonValidator {
    private final MonRepository monRepository;

    public void validateAdd(MonRequest request) {

        String tenMon = request.getTenMon();

        if (ValidateUtil.hasLeadingOrTrailingSpace(tenMon)) {
            throw new RuntimeException("Tên món không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(tenMon)) {
            throw new RuntimeException("Tên món không được chứa nhiều khoảng trắng liên tiếp");
        }
        if (ValidateUtil.hasMultipleSpaces(tenMon)) {
            throw new RuntimeException("Tên món không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (monRepository.existsByTenMonIgnoreCase(tenMon.trim())) {
            throw new RuntimeException("Tên món đã tồn tại");
        }
    }

    public void validateUpdate(Integer idMon, MonRequest request) {

        String tenMon = request.getTenMon();

        Mon monTrungTen = monRepository.findByTenMonIgnoreCase(tenMon.trim());

        if (monTrungTen != null
                && !monTrungTen.getIdMon().equals(idMon)) {

            throw new RuntimeException("Tên món đã tồn tại");
        }
    }
}
