package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComboRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietCombo;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
// 1. Kiểm tra danh sách món ăn chọn từ Checkbox Modal
        List<Integer> listIdMon = request.getDanhSachIdMon();
        if (listIdMon != null && !listIdMon.isEmpty()) {
            for (Integer idMon : listIdMon) {
                if (repo.existsByCombo_IdComboAndMon_IdMon(request.getIdCombo(), idMon)) {
                    throw new RuntimeException("Trong các món đã chọn, có món đã tồn tại trong combo này rồi");
                }
            }
        }
        // 2. Fallback nếu FE truyền 1 idMon lẻ
        else if (request.getIdMon() != null) {
            if (repo.existsByCombo_IdComboAndMon_IdMon(request.getIdCombo(), request.getIdMon())) {
                throw new RuntimeException("Món này đã tồn tại trong combo");
            }
        } else {
            throw new RuntimeException("Vui lòng chọn ít nhất một món ăn");
        }
    }

    public void validateUpdate(Integer idChiTietCombo,
                               ChiTietComboRequest request) {

        String moTa = request.getMoTa();

        if (moTa != null) {
            if (ValidateUtil.hasLeadingOrTrailingSpace(moTa)) {
                throw new RuntimeException("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối");
            }

            if (ValidateUtil.hasMultipleSpaces(moTa)) {
                throw new RuntimeException("Mô tả không được chứa nhiều khoảng trắng liên tiếp");
            }
        }

        // Khi sửa, cho phép chọn thêm món chưa có trong combo
        List<Integer> listIdMon = request.getDanhSachIdMon();
        if (listIdMon != null && !listIdMon.isEmpty()) {
            for (Integer idMon : listIdMon) {
                ChiTietCombo ctcbTrung = repo.findByCombo_IdComboAndMon_IdMon(request.getIdCombo(), idMon);
                // Nếu món này đã thuộc về 1 bản ghi ChiTietCombo KHÁC bản ghi đang sửa -> Báo lỗi
                if (ctcbTrung != null && !ctcbTrung.getIdChiTietCombo().equals(idChiTietCombo)) {
                    // Cho phép bỏ qua nếu nó là món đang sửa hoặc món mới tinh
                }
            }
        }
    }
}
