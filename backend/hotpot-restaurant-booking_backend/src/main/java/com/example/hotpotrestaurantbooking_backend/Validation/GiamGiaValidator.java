package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class GiamGiaValidator {

    public void validateAdd(GiamGiaRequest request) {
        String ma = request.getMaGiamGia();
        String dieuKien = request.getDieuKienSuDung();

        if (ValidateUtil.hasLeadingOrTrailingSpace(ma)) {
            throw new RuntimeException("Mã giảm giá không được chứa khoảng trắng đầu hoặc cuối");
        }
        if (ValidateUtil.hasMultipleSpaces(ma)) {
            throw new RuntimeException("Mã giảm giá không được chứa nhiều khoảng trắng liên tiếp");
        }
        if (ValidateUtil.hasLeadingOrTrailingSpace(dieuKien)) {
            throw new RuntimeException("Điều kiện sử dụng không được chứa khoảng trắng đầu hoặc cuối");
        }
        if (ValidateUtil.hasMultipleSpaces(dieuKien)) {
            throw new RuntimeException("Điều kiện sử dụng không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (request.getNgayKetThuc().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn hoặc bằng ngày hiện tại");
        }

        if (request.getGiaTriGiamToiDa().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm tối đa phải lớn hơn 0");
        }

        if (request.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if (request.getGiaTriGiamToiDa().compareTo(request.getGiaTriGiam()) < 0) {
            throw new RuntimeException("Giá trị giảm tối đa không được nhỏ hơn giá trị giảm");
        }

        if (request.getNgayKetThuc().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày kết thúc phải không được nhỏ hơn ngày hiện tại");
        }
    }

    public void validateUpdate(Integer idGiamGia, GiamGiaRequest request, LocalDate existingNgayTao) {
        String ma = request.getMaGiamGia();
        String dieuKien = request.getDieuKienSuDung();

        if (ValidateUtil.hasLeadingOrTrailingSpace(ma)) {
            throw new RuntimeException("Mã giảm giá không được chứa khoảng trắng đầu hoặc cuối");
        }
        if (ValidateUtil.hasMultipleSpaces(ma)) {
            throw new RuntimeException("Mã giảm giá không được chứa nhiều khoảng trắng liên tiếp");
        }
        if (ValidateUtil.hasLeadingOrTrailingSpace(dieuKien)) {
            throw new RuntimeException("Điều kiện sử dụng không được chứa khoảng trắng đầu hoặc cuối");
        }
        if (ValidateUtil.hasMultipleSpaces(dieuKien)) {
            throw new RuntimeException("Điều kiện sử dụng không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (request.getGiaTriGiamToiDa().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm tối đa phải lớn hơn 0");
        }

        if (request.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if (request.getGiaTriGiamToiDa().compareTo(request.getGiaTriGiam()) < 0) {
            throw new RuntimeException("Giá trị giảm tối đa không được nhỏ hơn giá trị giảm");
        }

        if (request.getNgayKetThuc().isBefore(existingNgayTao)) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn hoặc bằng ngày tạo mã giảm giá");
        }
    }
}
