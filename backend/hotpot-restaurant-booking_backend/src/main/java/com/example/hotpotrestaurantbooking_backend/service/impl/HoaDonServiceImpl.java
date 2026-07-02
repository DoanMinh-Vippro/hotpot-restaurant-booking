package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

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

        applyBanStatus(hd);

        hoaDonRepository.save(hd);
        return convertToResponse(hd);
    }

    @Override
    public DTOHoaDonResponse update(Integer id, DTOHoaDonRequest request) {
        hoaDonValidator.validateUpdate(id, request);
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + id));
        updateEntityFromRequest(hd, request);

        applyBanStatus(hd);

        hoaDonRepository.save(hd);
        return convertToResponse(hd);
    }

    @Override
    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    @Override
    public List<DTOHoaDonResponse> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAll();
        }
        return hoaDonRepository.findAll()
                .stream()
                .filter(hoaDon -> 
                    (hoaDon.getMaHoaDon() != null && hoaDon.getMaHoaDon().toLowerCase().contains(keyword.toLowerCase())) ||
                    (hoaDon.getSdtKhachHang() != null && hoaDon.getSdtKhachHang().toLowerCase().contains(keyword.toLowerCase())) ||
                    (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getTenKhachHang() != null && 
                     hoaDon.getKhachHang().getTenKhachHang().toLowerCase().contains(keyword.toLowerCase())) ||
                    (hoaDon.getMaGiaoDich() != null && hoaDon.getMaGiaoDich().toLowerCase().contains(keyword.toLowerCase()))
                )
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<DTOHoaDonResponse> findByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findAll()
                .stream()
                .filter(hoaDon -> matchesKhachHang(hoaDon, khachHangId))
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonResponse findByBanAndStatus(Integer idBan, Integer trangThaiHoaDon) {

        return hoaDonRepository
                .findByBan_IdBanAndTrangThaiHoaDon(idBan, trangThaiHoaDon)
                .map(this::convertToResponse)
                .orElse(null);
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

            if (hd.getKhachHang() == null && datBan.getKhachHang() != null) {
                hd.setKhachHang(datBan.getKhachHang());
            }
            if (hd.getSdtKhachHang() == null && datBan.getSdtKhachHang() != null) {
                hd.setSdtKhachHang(datBan.getSdtKhachHang());
            }
            if (hd.getTienCoc() == null && datBan.getSoTienCoc() != null) {
                hd.setTienCoc(datBan.getSoTienCoc());
            }
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

        if (hd.getKhachHang() == null && request.getIdKhachHang() == null && hd.getDatBan() != null && hd.getDatBan().getKhachHang() != null) {
            hd.setKhachHang(hd.getDatBan().getKhachHang());
        }
        if ((hd.getSdtKhachHang() == null || hd.getSdtKhachHang().isBlank()) && hd.getDatBan() != null && hd.getDatBan().getSdtKhachHang() != null) {
            hd.setSdtKhachHang(hd.getDatBan().getSdtKhachHang());
        }
        if (hd.getTienCoc() == null && hd.getDatBan() != null && hd.getDatBan().getSoTienCoc() != null) {
            hd.setTienCoc(hd.getDatBan().getSoTienCoc());
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

        attachCurrentNhanVienIfAbsent(hd);
    }

    private void attachCurrentNhanVienIfAbsent(HoaDon hd) {
        if (hd.getNhanVien() != null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return;
        }

        Object idTaiKhoanObj = jwt.getClaim("idTaiKhoan");
        if (idTaiKhoanObj instanceof Number number) {
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_IdTaiKhoan(number.intValue());
            if (nhanVien != null) {
                hd.setNhanVien(nhanVien);
            }
        }
    }

    private boolean matchesKhachHang(HoaDon hoaDon, Integer khachHangId) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getIdKhachHang() != null
                && hoaDon.getKhachHang().getIdKhachHang().equals(khachHangId)) {
            return true;
        }

        return hoaDon.getDatBan() != null
                && hoaDon.getDatBan().getKhachHang() != null
                && hoaDon.getDatBan().getKhachHang().getIdKhachHang() != null
                && hoaDon.getDatBan().getKhachHang().getIdKhachHang().equals(khachHangId);
    }

    private void applyBanStatus(HoaDon hd) {
        if (hd.getBan() == null) {
            return;
        }

        hd.getBan().setTrangThai(hd.getTrangThaiThanhToan() == 1 ? TrangThaiBan.TRONG : TrangThaiBan.DANG_SU_DUNG);
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
        if (response.getSdtKhachHang() == null && hoaDon.getDatBan() != null && hoaDon.getDatBan().getSdtKhachHang() != null) {
            response.setSdtKhachHang(hoaDon.getDatBan().getSdtKhachHang());
        }
        response.setTienTruocGiam(hoaDon.getTienTruocGiam());
        response.setTienCoc(hoaDon.getTienCoc());
        if (response.getTienCoc() == null && hoaDon.getDatBan() != null && hoaDon.getDatBan().getSoTienCoc() != null) {
            response.setTienCoc(hoaDon.getDatBan().getSoTienCoc());
        }
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
        } else if (hoaDon.getDatBan() != null && hoaDon.getDatBan().getKhachHang() != null) {
            response.setIdKhachHang(hoaDon.getDatBan().getKhachHang().getIdKhachHang());
            response.setTenKhachHang(hoaDon.getDatBan().getKhachHang().getTenKhachHang());
        }
        if (response.getIdKhachHang() == null && hoaDon.getDatBan() != null && hoaDon.getDatBan().getKhachHang() != null) {
            response.setIdKhachHang(hoaDon.getDatBan().getKhachHang().getIdKhachHang());
        }
        if (response.getTenKhachHang() == null && hoaDon.getDatBan() != null && hoaDon.getDatBan().getKhachHang() != null) {
            response.setTenKhachHang(hoaDon.getDatBan().getKhachHang().getTenKhachHang());
        }
        if (hoaDon.getNhanVien() != null) {
            response.setIdNhanVien(hoaDon.getNhanVien().getId());
            response.setTenNhanVien(hoaDon.getNhanVien().getTenNhanVien());
        }

        response.setChiTiet(
                hoaDonChiTietRepository.findByHoaDon_IdHoaDon(hoaDon.getIdHoaDon())
                        .stream()
                        .map(ct -> {
                            DTOHoaDonChiTietResponse dto = new DTOHoaDonChiTietResponse();
                            dto.setIdHoaDonChiTiet(ct.getIdHoaDonChiTiet());
                            dto.setIdMon(ct.getMon() != null ? ct.getMon().getIdMon() : null);
                            dto.setIdCombo(ct.getCombo() != null ? ct.getCombo().getIdCombo() : null);
                            dto.setTenMon(ct.getMon() != null ? ct.getMon().getTenMon() : null);
                            dto.setTenCombo(ct.getCombo() != null ? ct.getCombo().getTenCombo() : null);
                            dto.setSoLuong(ct.getSoLuong());
                            dto.setGiaBanTaiThoiDiem(ct.getGiaBanTaiThoiDien());
                            dto.setThanhTien(ct.getThanhTien());
                            return dto;
                        })
                        .toList()
        );

        return response;
    }
}
