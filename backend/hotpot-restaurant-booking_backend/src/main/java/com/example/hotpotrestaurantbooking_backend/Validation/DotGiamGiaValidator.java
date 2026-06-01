package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DotGiamGiaValidator {

    public void validateAdd(DotGiamGiaRequest request) {

        String ten = request.getTenChuongTrinh();

        // validate format string
        if (ValidateUtil.hasLeadingOrTrailingSpace(ten)) {
            throw new RuntimeException("Tên chương trình không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(ten)) {
            throw new RuntimeException("Tên chương trình không được chứa nhiều khoảng trắng liên tiếp");
        }

        // BUSINESS LOGIC DATE
        if (request.getNgayBatDau().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày bắt đầu không được nhỏ hơn ngày hiện tại");
        }

        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }
    }

    // ==========================
    // UPDATE
    // ==========================
    public void validateUpdate(Integer idDotGiamGia, DotGiamGiaRequest request) {

        String ten = request.getTenChuongTrinh();

        // validate format string
        if (ValidateUtil.hasLeadingOrTrailingSpace(ten)) {
            throw new RuntimeException("Tên chương trình không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(ten)) {
            throw new RuntimeException("Tên chương trình không được chứa nhiều khoảng trắng liên tiếp");
        }

        // BUSINESS LOGIC DATE
        // update KHÔNG chặn ngày bắt đầu < hôm nay (tránh khóa dữ liệu cũ)
        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }
    }
}
