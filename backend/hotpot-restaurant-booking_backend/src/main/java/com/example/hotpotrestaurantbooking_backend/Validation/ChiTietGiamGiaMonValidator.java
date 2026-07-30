package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChiTietGiamGiaMonValidator {
    private final ChiTietGiamGiaMonRepository chiTietRepository;
    private final MonRepository monRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;

    // ====== ADD ======
    public void validateAdd(ChiTietGiamGiaMonRequest request) {

        Integer idDotGiamGia = request.getIdDotGiamGia();
        BigDecimal mucGiam = request.getMucGiam();

        // 1. Check Đợt giảm giá FK
        if (idDotGiamGia == null || !dotGiamGiaRepository.existsById(idDotGiamGia)) {
            throw new RuntimeException("Đợt giảm giá không tồn tại");
        }

        // 2. Gom danh sách ID món cần validate (hỗ trợ cả mảng lẫn 1 món lẻ)
        List<Integer> listMonId = new ArrayList<>();
        if (request.getDanhSachMonId() != null && !request.getDanhSachMonId().isEmpty()) {
            listMonId.addAll(request.getDanhSachMonId());
        } else if (request.getIdMon() != null) {
            listMonId.add(request.getIdMon());
        } else {
            throw new RuntimeException("Vui lòng chọn ít nhất một món ăn");
        }

        // 3. Validate Mức giảm & Loại giảm cơ bản
        validateCommonInfo(mucGiam, request.getLoaiGiam());

        String loaiGiam = request.getLoaiGiam().trim().toUpperCase();

        // 4. Lặp qua từng món để validate logic kinh doanh & check trùng toàn hệ thống
        for (Integer idMon : listMonId) {
            Mon mon = monRepository.findById(idMon)
                    .orElseThrow(() -> new RuntimeException("Món ăn có ID: " + idMon + " không tồn tại"));

            // 🔥 CHECK TRÙNG: 1 Món chỉ được dùng duy nhất 1 chương trình giảm giá trên toàn hệ thống
            if (chiTietRepository.existsByMon_IdMon(idMon)) {
                throw new RuntimeException("Món [" + mon.getTenMon() + "] đã được áp dụng trong một chương trình giảm giá khác rồi!");
            }

            // Check tiền giảm không lớn hơn giá món hiện tại
            if (loaiGiam.equals("TIEN") && mucGiam.compareTo(mon.getDonGiaHienTai()) > 0) {
                throw new RuntimeException("Số tiền giảm không được lớn hơn giá trị món [" + mon.getTenMon() + "]");
            }
        }
    }

    // ====== UPDATE ======
    public void validateUpdate(Integer idChiTiet, ChiTietGiamGiaMonRequest request) {

        Integer idDotGiamGia = request.getIdDotGiamGia();
        BigDecimal mucGiam = request.getMucGiam();

        if (idDotGiamGia == null || !dotGiamGiaRepository.existsById(idDotGiamGia)) {
            throw new RuntimeException("Đợt giảm giá không tồn tại");
        }

        // Với Update, lấy ra món cần sửa
        Integer idMon = request.getIdMon();
        if (idMon == null && request.getDanhSachMonId() != null && !request.getDanhSachMonId().isEmpty()) {
            idMon = request.getDanhSachMonId().get(0);
        }

        if (idMon == null) {
            throw new RuntimeException("Món ăn không được để trống");
        }

        Mon mon = monRepository.findById(idMon)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        // 🔥 CHECK TRÙNG KHI UPDATE: Bỏ qua ID bản ghi hiện tại đang sửa
        boolean isExist = chiTietRepository.existsByMon_IdMonAndIdChiTietGiamGiaMonNot(idMon, idChiTiet);
        if (isExist) {
            throw new RuntimeException("Món [" + mon.getTenMon() + "] đã đang nằm trong một mã giảm giá khác!");
        }

        validateCommonInfo(mucGiam, request.getLoaiGiam());

        String loaiGiam = request.getLoaiGiam().trim().toUpperCase();
        if (loaiGiam.equals("TIEN") && mucGiam.compareTo(mon.getDonGiaHienTai()) > 0) {
            throw new RuntimeException("Số tiền giảm không được lớn hơn giá trị món [" + mon.getTenMon() + "]");
        }
    }

    // ====== HÀM CHUNG VALIDATE MỨC GIẢM VÀ LOẠI GIẢM ======
    private void validateCommonInfo(BigDecimal mucGiam, String rawLoaiGiam) {
        if (mucGiam == null) {
            throw new RuntimeException("Mức giảm không được để trống");
        }

        if (mucGiam.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mức giảm phải lớn hơn 0");
        }

        if (ValidateUtil.isBlank(rawLoaiGiam)) {
            throw new RuntimeException("Loại giảm không được để trống");
        }

        String loaiGiam = rawLoaiGiam.trim().toUpperCase();
        if (!loaiGiam.equals("TIEN") && !loaiGiam.equals("PHANTRAM")) {
            throw new RuntimeException("Loại giảm chỉ được là TIEN hoặc PHANTRAM");
        }

        if (loaiGiam.equals("PHANTRAM") && mucGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Phần trăm giảm không được vượt quá 100%");
        }
    }
}
