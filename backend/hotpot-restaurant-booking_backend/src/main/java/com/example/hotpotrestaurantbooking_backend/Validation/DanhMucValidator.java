package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResquest;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import com.example.hotpotrestaurantbooking_backend.repository.DanhMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DanhMucValidator {
    private final DanhMucRepository repo;

    // ====== ADD ======
    public void validateAdd(DanhMucResquest req) {

        String loaiDanhMuc = req.getLoaiDanhMuc();
        String moTa = req.getMoTa();

        if (ValidateUtil.hasLeadingOrTrailingSpace(loaiDanhMuc)) {
            throw new RuntimeException("Loại danh mục không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(loaiDanhMuc)) {
            throw new RuntimeException("Loại danh mục không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (repo.existsByLoaiDanhMucIgnoreCase(loaiDanhMuc.trim())) {
            throw new RuntimeException("Loại danh mục đã tồn tại");
        }

        if (ValidateUtil.hasLeadingOrTrailingSpace(moTa)) {
            throw new RuntimeException("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(moTa)) {
            throw new RuntimeException("Mô tả không được chứa nhiều khoảng trắng liên tiếp");
        }
    }

    // ====== UPDATE ======
    public void validateUpdate(Integer idDanhMuc, DanhMucResquest req) {

        String loaiDanhMuc = req.getLoaiDanhMuc();
        String moTa = req.getMoTa();

        DanhMuc danhMucTrung = repo.findByLoaiDanhMucIgnoreCase(loaiDanhMuc.trim());

        if (danhMucTrung != null
                && !danhMucTrung.getIdDanhMuc().equals(idDanhMuc)) {

            throw new RuntimeException("Loại danh mục đã tồn tại");
        }

        if (ValidateUtil.hasLeadingOrTrailingSpace(loaiDanhMuc)) {
            throw new RuntimeException("Loại danh mục không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(loaiDanhMuc)) {
            throw new RuntimeException("Loại danh mục không được chứa nhiều khoảng trắng liên tiếp");
        }

        if (ValidateUtil.hasLeadingOrTrailingSpace(moTa)) {
            throw new RuntimeException("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối");
        }

        if (ValidateUtil.hasMultipleSpaces(moTa)) {
            throw new RuntimeException("Mô tả không được chứa nhiều khoảng trắng liên tiếp");
        }
    }
}
