package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.Validation.ValidateUtil;
import com.example.hotpotrestaurantbooking_backend.dto.Mon.MonRequest;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonValidator {
    private final MonRepository monRepository;

    public void validate(MonRequest request) {

        String tenMon = request.getTenMon();

        if (ValidateUtil.hasLeadingOrTrailingSpace(tenMon)) {
            throw new RuntimeException(
                    "Tên món không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(tenMon)) {
            throw new RuntimeException(
                    "Tên món không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (monRepository.existsByTenMonIgnoreCase(tenMon.trim())) {
            throw new RuntimeException(
                    "Tên món đã tồn tại");
        }
    }
}
