package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final BanRepository banRepository;
    private final DatBanRepository datBanRepository;
    private final GiamGiaRepository giamGiaRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final HoaDonValidator hoaDonValidator;

    @Override
    public List<DTOHoaDonResponse> getAll() {
        return hoaDonRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonResponse findById(Integer id) {
        return hoaDonRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + id));
    }

    @Override
    public DTOHoaDonResponse add(DTOHoaDonRequest request) {
        hoaDonValidator.validateAdd(request);
        HoaDon hd = new HoaDon();
        updateEntityFromRequest(hd, request);
        hoaDonRepository.save(hd);
        return convertToResponse(hd);
    }

    @Override
    public DTOHoaDonResponse update(Integer id, DTOHoaDonRequest request) {
        hoaDonValidator.validateUpdate(id, request);
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + id));
        updateEntityFromRequest(hd, request);
        hoaDonRepository.save(hd);
        return convertToResponse(hd);
    }

    @Override
    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    private void updateEntityFromRequest(HoaDon hd, DTOHoaDonRequest request) {
        if (request.getMaHoaDon() != null) hd.setMaHoaDon(request.getMaHoaDon());
        if (request.getMaGiaoDich() != null) hd.setMaGiaoDich(request.getMaGiaoDich());
        if (request.getTrangThaiHoaDon() != null) hd.setTrangThaiHoaDon(request.getTrangThaiHoaDon());
        if (request.getSdtKhachHang() != null) hd.setSdtKhachHang(request.getSdtKhachHang());
        if (request.getTienTruocGiam() != null) hd.setTienTruocGiam(request.getTienTruocGiam());
        if (request.getTienCoc() != null) hd.setTienCoc(request.getTienCoc());
        if (request.getTienGiamGia() != null) hd.setTienGiamGia(request.getTienGiamGia());
        if (request.getTongTien() != null) hd.setTongTien(request.getTongTien());
        if (request.getThoiGianXuat() != null) hd.setThoiGianXuat(request.getThoiGianXuat());
        if (request.getTrangThaiThanhToan() != null) hd.setTrangThaiThanhToan(request.getTrangThaiThanhToan());
        if (request.getPhuongThucThanhToan() != null) hd.setPhuongThucThanhToan(request.getPhuongThucThanhToan());

        if (request.getIdBan() != null) {
            Ban ban = banRepository.findById(request.getIdBan())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay ban"));
            hd.setBan(ban);
        }

        if (request.getIdDatBan() != null) {
            DatBan datBan = datBanRepository.findById(request.getIdDatBan())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay thong tin dat ban"));
            hd.setDatBan(datBan);
        }

        GiamGia selectedGiamGia = null;
        if (request.getIdGiamGia() != null) {
            selectedGiamGia = giamGiaRepository.findById(request.getIdGiamGia())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay ma giam gia"));
            hd.setGiamGia(selectedGiamGia);
        }

        if (request.getIdKhachHang() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay khach hang"));
            hd.setKhachHang(khachHang);
        }

        if (request.getTienGiamGia() == null && selectedGiamGia != null && hd.getTienTruocGiam() != null) {
            hd.setTienGiamGia(applyGiamGiaDiscount(hd.getTienTruocGiam(), selectedGiamGia));
        }

        if (hd.getTienTruocGiam() != null && hd.getTienGiamGia() != null && request.getTongTien() == null) {
            hd.setTongTien(hd.getTienTruocGiam().subtract(hd.getTienGiamGia()).max(BigDecimal.ZERO));
        }

        if (request.getIdNhanVien() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVien())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay nhan vien"));
            hd.setNhanVien(nhanVien);
        }
    }

    private BigDecimal applyGiamGiaDiscount(BigDecimal tienTruocGiam, GiamGia giamGia) {
        if (tienTruocGiam == null || giamGia == null) {
            return BigDecimal.ZERO;
        }

        if ("PHAN_TRAM".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            BigDecimal percent = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            BigDecimal discount = tienTruocGiam.multiply(percent).divide(BigDecimal.valueOf(100));
            if (giamGia.getGiaTriGiamToiDa() != null) {
                discount = discount.min(giamGia.getGiaTriGiamToiDa());
            }
            return discount.max(BigDecimal.ZERO);
        }

        if ("TIEN_MAT".equalsIgnoreCase(giamGia.getLoaiGiam())) {
            BigDecimal fixedDiscount = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            if (giamGia.getGiaTriGiamToiDa() != null) {
                fixedDiscount = fixedDiscount.min(giamGia.getGiaTriGiamToiDa());
            }
            return fixedDiscount.max(BigDecimal.ZERO);
        }

        return BigDecimal.ZERO;
    }

    private DTOHoaDonResponse convertToResponse(HoaDon hoaDon) {
        DTOHoaDonResponse response = new DTOHoaDonResponse();
        response.setIdHoaDon(hoaDon.getIdHoaDon());
        response.setMaHoaDon(hoaDon.getMaHoaDon());
        response.setMaGiaoDich(hoaDon.getMaGiaoDich());
        response.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());
        response.setSdtKhachHang(hoaDon.getSdtKhachHang());
        response.setTienTruocGiam(hoaDon.getTienTruocGiam());
        response.setTienCoc(hoaDon.getTienCoc());
        response.setTienGiamGia(hoaDon.getTienGiamGia());
        response.setTongTien(hoaDon.getTongTien());
        response.setThoiGianXuat(hoaDon.getThoiGianXuat());
        response.setTrangThaiThanhToan(hoaDon.getTrangThaiThanhToan());
        response.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        if (hoaDon.getBan() != null) {
            response.setIdBan(hoaDon.getBan().getIdBan());
            response.setLoaiBan(hoaDon.getBan().getLoaiBan());
        }
        if (hoaDon.getDatBan() != null) {
            response.setIdDatBan(hoaDon.getDatBan().getIdDatBan());
        }
        if (hoaDon.getGiamGia() != null) {
            response.setIdGiamGia(hoaDon.getGiamGia().getIdGiamGia());
            response.setMaGiamGia(hoaDon.getGiamGia().getMaGiamGia());
            response.setLoaiGiam(hoaDon.getGiamGia().getLoaiGiam());
        }
        if (hoaDon.getKhachHang() != null) {
            response.setIdKhachHang(hoaDon.getKhachHang().getIdKhachHang());
            response.setTenKhachHang(hoaDon.getKhachHang().getTenKhachHang());
        }
        if (hoaDon.getNhanVien() != null) {
            response.setIdNhanVien(hoaDon.getNhanVien().getId());
            response.setTenNhanVien(hoaDon.getNhanVien().getTenNhanVien());
        }
        return response;
    }
}
