package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.CocResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.service.CocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Tự động inject DatBanRepository sạch sẽ qua Constructor
public class CocServiceImpl implements CocService {

    private final DatBanRepository datBanRepository;

    @Override
    public List<CocResponse> findAll() {
        // Sử dụng hàm GreaterThan truyền vào 0 thay cho @Query
        return datBanRepository.findBySoTienCocGreaterThan(BigDecimal.ZERO).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CocResponse> findByTrangThaiCoc(Integer trangThaiCoc) {
        return datBanRepository.findByTrangThaiCoc(trangThaiCoc).stream()
                .filter(d -> d.getSoTienCoc() != null && d.getSoTienCoc().compareTo(BigDecimal.ZERO) > 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CocResponse> findByKhachHang(Integer khachHangId) {
        return datBanRepository.findByKhachHangIdKhachHang(khachHangId).stream()
                .filter(d -> d.getSoTienCoc() != null && d.getSoTienCoc().compareTo(BigDecimal.ZERO) > 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal tongTienCocDaThu() {
        // Gọi thẳng hàm tính tổng an toàn từ DB, không lo sập lỗi 500 nữa
        return datBanRepository.sumTongTienCocDaThu();
    }

    private CocResponse convertToResponse(DatBan datBan) {
        CocResponse response = new CocResponse();
        response.setId(datBan.getIdDatBan());
        response.setNgayDat(datBan.getNgayDat());
        response.setGioDat(datBan.getGioDat());
        response.setSoNguoi(datBan.getSoNguoi());
        response.setSoTienCoc(datBan.getSoTienCoc());
        response.setGhiChu(datBan.getGhiChu());

        if (datBan.getKhachHang() != null) {
            response.setMaKhachHang(datBan.getKhachHang().getMaKhachHang());
            response.setTenKhachHang(datBan.getKhachHang().getTenKhachHang());
            response.setSoDienThoai(datBan.getKhachHang().getSoDienThoai());
        }

        // === ĐOẠN FIX CHO TRẠNG THÁI CỌC ===
        if (datBan.getTrangThaiCoc() != null) {
            // Chuyển Enum thành số Integer (0, 1) để lưu vào DTO
            response.setTrangThaiCoc(datBan.getTrangThaiCoc().ordinal());

            // Tự động map chữ Tiếng Việt dựa theo tên Enum của nhóm
            switch (datBan.getTrangThaiCoc().name()) {
                case "DA_THANH_TOAN":
                case "DA_COC":
                    response.setTrangThaiCocText("Đã cọc");
                    break;
                default:
                    response.setTrangThaiCocText("Chưa cọc");
            }
        } else {
            response.setTrangThaiCoc(0);
            response.setTrangThaiCocText("Chưa cọc");
        }

        // === ĐOẠN FIX CHO PHƯƠNG THỨC THANH TOÁN ===
        if (datBan.getPhuongThucThanhToan() != null) {
            // Chuyển Enum thành số Integer để lưu vào DTO
            response.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan().ordinal());

            switch (datBan.getPhuongThucThanhToan().name()) {
                case "TIEN_MAT": response.setPhuongThucThanhToanText("Tiền mặt"); break;
                case "CHUYEN_KHOAN": response.setPhuongThucThanhToanText("Chuyển khoản"); break;
                case "THE": response.setPhuongThucThanhToanText("Thẻ"); break;
                default: response.setPhuongThucThanhToanText("Chưa chọn");
            }
        } else {
            response.setPhuongThucThanhToanText("Chưa chọn");
        }

        return response;
    }
}