package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
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

        // Chuyển trạng thái bàn khi tạo hóa đơn lần đầu
        if (hd.getBan() != null) {
            applyTableStateFromInvoice(hd.getBan(), hd);
            banRepository.save(hd.getBan());
        }

        hoaDonRepository.save(hd);
        return convertToResponse(hd);
    }

    @Override
    public DTOHoaDonResponse update(Integer id, DTOHoaDonRequest request) {
        hoaDonValidator.validateUpdate(id, request);
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + id));
        updateEntityFromRequest(hd, request);

        // Chỉ trả bàn khi hóa đơn đã hoàn tất hoặc đã thanh toán
        if (hd.getBan() != null) {
            applyTableStateFromInvoice(hd.getBan(), hd);
            banRepository.save(hd.getBan());
        }

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
                .filter(hoaDon -> hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getIdKhachHang().equals(khachHangId))
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonResponse findByBanAndStatus(Integer idBan, Integer trangThaiHoaDon) {
        if (idBan == null || trangThaiHoaDon == null) {
            return null;
        }

        Ban ban = banRepository.findById(idBan).orElse(null);
        if (ban != null && ban.getTrangThai() == TrangThaiBan.TRONG) {
            return null;
        }

        Integer pendingPaymentStatus = 0;
        return hoaDonRepository
                .findFirstByBan_IdBanAndTrangThaiHoaDonAndTrangThaiThanhToan(idBan, trangThaiHoaDon, pendingPaymentStatus)
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

        DatBan resolvedDatBan = null;
        if (request.getIdDatBan() != null) {
            resolvedDatBan = datBanRepository.findById(request.getIdDatBan())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay thong tin dat ban"));
            hd.setDatBan(resolvedDatBan);
        }

        if (resolvedDatBan != null) {
            if (request.getIdKhachHang() == null && resolvedDatBan.getKhachHang() != null) {
                hd.setKhachHang(resolvedDatBan.getKhachHang());
            }
            if (request.getSdtKhachHang() == null && resolvedDatBan.getSdtKhachHang() != null) {
                hd.setSdtKhachHang(resolvedDatBan.getSdtKhachHang());
            }
            if (request.getTienCoc() == null && resolvedDatBan.getSoTienCoc() != null) {
                hd.setTienCoc(resolvedDatBan.getSoTienCoc());
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

    private boolean isInvoicePaid(HoaDon hoaDon) {
        boolean paidByPaymentStatus = hoaDon.getTrangThaiThanhToan() != null && hoaDon.getTrangThaiThanhToan() == 1;
        boolean completedByInvoiceStatus = hoaDon.getTrangThaiHoaDon() != null && hoaDon.getTrangThaiHoaDon() == 1;
        return paidByPaymentStatus || completedByInvoiceStatus;
    }

    private void applyTableStateFromInvoice(Ban ban, HoaDon hoaDon) {
        if (ban == null) {
            return;
        }

        if (Boolean.TRUE.equals(isInvoicePaid(hoaDon))) {
            ban.setTrangThai(TrangThaiBan.TRONG);
        } else {
            ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG);
        }
    }

    private BigDecimal applyGiamGiaDiscount(BigDecimal tienTruocGiam, GiamGia giamGia) {
        if (tienTruocGiam == null || giamGia == null) {
            return BigDecimal.ZERO;
        }

        String normalizedType = normalizeDiscountType(giamGia.getLoaiGiam());

        if ("PERCENT".equals(normalizedType)) {
            BigDecimal percent = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            BigDecimal discount = tienTruocGiam.multiply(percent).divide(BigDecimal.valueOf(100));
            if (giamGia.getGiaTriGiamToiDa() != null) {
                discount = discount.min(giamGia.getGiaTriGiamToiDa());
            }
            return discount.max(BigDecimal.ZERO);
        }

        if ("FIXED".equals(normalizedType)) {
            BigDecimal fixedDiscount = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            if (giamGia.getGiaTriGiamToiDa() != null) {
                fixedDiscount = fixedDiscount.min(giamGia.getGiaTriGiamToiDa());
            }
            return fixedDiscount.max(BigDecimal.ZERO);
        }

        return BigDecimal.ZERO;
    }

    private String normalizeDiscountType(String rawType) {
        if (rawType == null) {
            return "UNKNOWN";
        }

        String normalized = Normalizer.normalize(rawType, Normalizer.Form.NFKD)
                .replaceAll("[\\p{M}]", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();

        if (normalized.contains("PHANTRAM") || normalized.contains("PERCENT")) {
            return "PERCENT";
        }

        if (normalized.contains("TIEN") || normalized.contains("MAT") || normalized.contains("GIATRI") || normalized.contains("VALUE")) {
            return "FIXED";
        }

        return "UNKNOWN";
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
            response.setTenBan(hoaDon.getBan().getTenBan());
        }

        if (hoaDon.getDatBan() != null) {
            response.setIdDatBan(hoaDon.getDatBan().getIdDatBan());
            response.setGioVaoBan(hoaDon.getDatBan().getThoiGianDenDuKien());
            if (hoaDon.getDatBan().getThoiGianDenDuKien() != null) {
                response.setGioRoiBan(hoaDon.getDatBan().getThoiGianDenDuKien().plusHours(2));
            }

            List<DTOBanResponse> dsBan = hoaDon.getDatBan().getChiTietDatBanBans() == null
                    ? List.of()
                    : hoaDon.getDatBan().getChiTietDatBanBans().stream()
                            .filter(ct -> ct != null && ct.getBan() != null)
                            .map(ct -> {
                                DTOBanResponse dto = new DTOBanResponse();
                                dto.setIdBan(ct.getBan().getIdBan());
                                dto.setTenBan(ct.getBan().getTenBan());
                                dto.setLoaiBan(ct.getBan().getLoaiBan());
                                return dto;
                            })
                            .distinct()
                            .toList();
            response.setDsBan(dsBan);
            if (!dsBan.isEmpty()) {
                response.setTenBan(dsBan.stream()
                        .map(DTOBanResponse::getTenBan)
                        .filter(name -> name != null && !name.isBlank())
                        .distinct()
                        .sorted()
                        .toList()
                        .stream()
                        .collect(java.util.stream.Collectors.joining(", ")));
            }
        }

        if (response.getGioVaoBan() == null) {
            response.setGioVaoBan(hoaDon.getThoiGianXuat());
        }
        if (response.getGioRoiBan() == null && response.getGioVaoBan() != null) {
            response.setGioRoiBan(response.getGioVaoBan().plusHours(2));
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
