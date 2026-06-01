package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.repository.GiamGiaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HoaDonValidator {

    private final GiamGiaRepository giamGiaRepository;

    public HoaDonValidator(GiamGiaRepository giamGiaRepository) {
        this.giamGiaRepository = giamGiaRepository;
    }

    public void validateAdd(DTOHoaDonRequest request) {
        validateCommon(request);
        if (ValidateUtil.isBlank(request.getMaHoaDon())) {
            throw new RuntimeException("Mã hóa đơn không được để trống");
        }
    }

    public void validateUpdate(Integer idHoaDon, DTOHoaDonRequest request) {
        validateCommon(request);
    }

    private void validateCommon(DTOHoaDonRequest request) {
        String maHoaDon = request.getMaHoaDon();
        if (maHoaDon != null) {
            if (ValidateUtil.hasLeadingOrTrailingSpace(maHoaDon)) {
                throw new RuntimeException("Mã hóa đơn không được chứa khoảng trắng ở đầu hoặc cuối");
            }
            if (ValidateUtil.hasMultipleSpaces(maHoaDon)) {
                throw new RuntimeException("Mã hóa đơn không được chứa nhiều khoảng trắng liên tiếp");
            }
        }

        BigDecimal tienTruocGiam = request.getTienTruocGiam();
        BigDecimal tienGiamGia = request.getTienGiamGia();
        BigDecimal tongTien = request.getTongTien();

        if (tienTruocGiam != null && tienTruocGiam.signum() < 0) {
            throw new RuntimeException("Tiền trước giảm không được âm");
        }

        if (tienGiamGia != null && tienGiamGia.signum() < 0) {
            throw new RuntimeException("Tiền giảm giá không được âm");
        }

        if (tongTien != null && tongTien.signum() < 0) {
            throw new RuntimeException("Tổng tiền không được âm");
        }

        if (tienGiamGia != null && tienTruocGiam != null && tienGiamGia.compareTo(tienTruocGiam) > 0) {
            throw new RuntimeException("Tiền giảm giá không được lớn hơn tiền trước giảm");
        }

        if (request.getIdGiamGia() != null) {
            if (!giamGiaRepository.existsById(request.getIdGiamGia())) {
                throw new RuntimeException("Không tìm thấy mã giảm giá");
            }

            if (tienTruocGiam != null && tienTruocGiam.signum() == 0) {
                throw new RuntimeException("Tiền trước giảm phải lớn hơn 0 để áp dụng mã giảm giá");
            }
        }
    }
}
