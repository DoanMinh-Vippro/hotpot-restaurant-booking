package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOSplitHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Transactional
    public DTOHoaDonResponse add(DTOHoaDonRequest request) {
        hoaDonValidator.validateAdd(request);
        HoaDon hd = new HoaDon();
        updateEntityFromRequest(hd, request);

        // Lưu hóa đơn trước, sau đó cập nhật trạng thái bàn dựa trên trạng thái hóa đơn đã lưu
        hoaDonRepository.save(hd);
        consumeDiscountIfNeeded(null, hd);

        if (hd.getBan() != null) {
            applyTableStateFromInvoice(hd.getBan(), hd);
            banRepository.save(hd.getBan());
        }
        if (hd.getDatBan() != null && isInvoicePaid(hd)) {
            completeDatBanAndFreeTables(hd.getDatBan());
        }

        return convertToResponse(hd, request.getChiTiet());
    }

    @Override
    @Transactional // 👈 THÊM ANNOTATION NÀY
    public DTOHoaDonResponse update(Integer id, DTOHoaDonRequest request) {
        hoaDonValidator.validateUpdate(id, request);
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + id));
        Integer previousPaymentStatus = hd.getTrangThaiThanhToan();
        Integer previousInvoiceStatus = hd.getTrangThaiHoaDon();
        Integer previousDiscountId = hd.getGiamGia() != null ? hd.getGiamGia().getIdGiamGia() : null;
        updateEntityFromRequest(hd, request);

        // Chỉ trả bàn khi hóa đơn đã hoàn tất hoặc đã thanh toán
        if (hd.getBan() != null) {
            applyTableStateFromInvoice(hd.getBan(), hd);
            banRepository.save(hd.getBan());
        }
        if (hd.getDatBan() != null && isInvoicePaid(hd)) {
            completeDatBanAndFreeTables(hd.getDatBan());
        }

        HoaDon saved = hoaDonRepository.save(hd);
        consumeDiscountIfNeeded(
                new InvoiceDiscountState(previousInvoiceStatus, previousPaymentStatus, previousDiscountId),
                saved
        );

        // Sau khi lưu, đảm bảo trạng thái bàn khớp với trạng thái hóa đơn (tránh race condition)
        if (saved.getBan() != null) {
            applyTableStateFromInvoice(saved.getBan(), saved);
            banRepository.save(saved.getBan());
        }
        if (saved.getDatBan() != null && isInvoicePaid(saved)) {
            completeDatBanAndFreeTables(saved.getDatBan());
        }

        return convertToResponse(saved, request.getChiTiet());
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
    public List<DTOHoaDonResponse> getActiveTableInvoices() {
        return hoaDonRepository.findByTrangThaiHoaDonAndTrangThaiThanhToanAndBanIsNotNull(0, 0)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonResponse findByBanAndStatus(Integer idBan, Integer trangThaiHoaDon) {
        if (idBan == null || trangThaiHoaDon == null) {
            return null;
        }

        Integer pendingPaymentStatus = 0;
        return hoaDonRepository
                .findFirstByBan_IdBanAndTrangThaiHoaDonAndTrangThaiThanhToan(idBan, trangThaiHoaDon, pendingPaymentStatus)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public DTOHoaDonResponse split(Integer idHoaDon, DTOSplitHoaDonRequest request) {
        HoaDon source = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don voi id: " + idHoaDon));
        if (source.getBan() == null || Objects.equals(source.getTrangThaiThanhToan(), 1)
                || Objects.equals(source.getTrangThaiHoaDon(), 1)) {
            throw new IllegalArgumentException("Chi co the tach hoa don dang mo cua ban");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Phai chon it nhat mot mon de tach");
        }

        List<HoaDonChiTiet> sourceItems = hoaDonChiTietRepository.findByHoaDon_IdHoaDon(idHoaDon);
        Map<Integer, HoaDonChiTiet> itemsById = sourceItems.stream()
                .collect(Collectors.toMap(HoaDonChiTiet::getIdHoaDonChiTiet, Function.identity()));
        HoaDon target = new HoaDon();
        target.setMaHoaDon("HD2" + idHoaDon + String.format("%06d", Math.floorMod(System.currentTimeMillis(), 1_000_000)));
        target.setMaGiaoDich(null);
        target.setTrangThaiHoaDon(0);
        target.setTrangThaiThanhToan(0);
        target.setPhuongThucThanhToan(null);
        target.setThoiGianXuat(LocalDateTime.now());
        target.setBan(source.getBan());
        target.setDatBan(source.getDatBan());
        target.setKhachHang(source.getKhachHang());
        target.setSdtKhachHang(source.getSdtKhachHang());
        target.setNhanVien(source.getNhanVien());
        target.setTienCoc(BigDecimal.ZERO);
        target.setGiamGia(null);
        hoaDonRepository.save(target);

        BigDecimal movedSubtotal = BigDecimal.ZERO;
        int movedQuantity = 0;
        int splitLineNumber = 0;
        for (DTOSplitHoaDonRequest.Item requested : request.getItems()) {
            if (requested == null || requested.getIdHoaDonChiTiet() == null
                    || requested.getSoLuong() == null || requested.getSoLuong() <= 0) {
                throw new IllegalArgumentException("So luong tach phai lon hon 0");
            }
            HoaDonChiTiet sourceItem = itemsById.get(requested.getIdHoaDonChiTiet());
            if (sourceItem == null) {
                throw new IllegalArgumentException("Mon tach khong thuoc hoa don goc");
            }
            int quantity = requested.getSoLuong();
            if (quantity > sourceItem.getSoLuong()) {
                throw new IllegalArgumentException("So luong tach vuot qua so luong hien co");
            }

            HoaDonChiTiet targetItem = new HoaDonChiTiet();
            targetItem.setMaHoaDonChiTiet("HDCT" + target.getIdHoaDon() + String.format("%02d", ++splitLineNumber));
            targetItem.setHoaDon(target);
            targetItem.setMon(sourceItem.getMon());
            targetItem.setCombo(sourceItem.getCombo());
            targetItem.setSoLuong(quantity);
            targetItem.setGiaBanTaiThoiDien(sourceItem.getGiaBanTaiThoiDien());
            targetItem.setTienGiamGiaMon(sourceItem.getTienGiamGiaMon());
            targetItem.setThanhTien(sourceItem.getGiaBanTaiThoiDien().multiply(BigDecimal.valueOf(quantity)));
            targetItem.setOrderedAt(sourceItem.getOrderedAt());
            targetItem.setOrderedBy(sourceItem.getOrderedBy());
            movedSubtotal = movedSubtotal.add(targetItem.getThanhTien());
            movedQuantity += quantity;

            int remaining = sourceItem.getSoLuong() - quantity;
            sourceItem.setSoLuong(remaining);
            sourceItem.setThanhTien(sourceItem.getGiaBanTaiThoiDien().multiply(BigDecimal.valueOf(remaining)));
            if (remaining == 0) {
                hoaDonChiTietRepository.delete(sourceItem);
            } else {
                hoaDonChiTietRepository.save(sourceItem);
            }
            hoaDonChiTietRepository.save(targetItem);
        }

        if (movedQuantity == 0 || hoaDonChiTietRepository.findByHoaDon_IdHoaDon(idHoaDon).stream()
                .mapToInt(HoaDonChiTiet::getSoLuong).sum() == 0) {
            throw new IllegalArgumentException("Hoa don goc va hoa don moi phai co mon");
        }
        target.setTienTruocGiam(movedSubtotal);
        target.setTienGiamGia(BigDecimal.ZERO);
        target.setTongTien(movedSubtotal);
        hoaDonRepository.save(target);

        BigDecimal remainingSubtotal = hoaDonChiTietRepository.findByHoaDon_IdHoaDon(idHoaDon).stream()
                .map(item -> item.getGiaBanTaiThoiDien().multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        source.setTienTruocGiam(remainingSubtotal);
        BigDecimal sourceDiscount = applyGiamGiaDiscount(remainingSubtotal, source.getGiamGia());
        source.setTienGiamGia(sourceDiscount);
        source.setTongTien(remainingSubtotal.subtract(sourceDiscount).subtract(safeMoney(source.getTienCoc())).max(BigDecimal.ZERO));
        hoaDonRepository.save(source);
        return convertToResponse(target);
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
        if (request.getSoTienChuyenKhoan() != null) hd.setSoTienChuyenKhoan(request.getSoTienChuyenKhoan());
        if (request.getSoTienTienMat() != null) hd.setSoTienTienMat(request.getSoTienTienMat());

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
        } else if (request.getTienGiamGia() != null && request.getTienGiamGia().compareTo(BigDecimal.ZERO) <= 0) {
            hd.setGiamGia(null);
        }

        if (request.getIdKhachHang() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay khach hang"));
            hd.setKhachHang(khachHang);
        }

        if (request.getIdNhanVien() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVien())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay nhan vien"));
            hd.setNhanVien(nhanVien);
        }

        syncInvoiceTotals(hd);
    }

    private boolean isInvoicePaid(HoaDon hoaDon) {
        if (hoaDon == null) return false;

        // Kiểm tra trạng thái thanh toán (1: Đã thanh toán)
        boolean paidByPaymentStatus = hoaDon.getTrangThaiThanhToan() != null && hoaDon.getTrangThaiThanhToan().equals(1);

        // Kiểm tra trạng thái hóa đơn (1: Đã hoàn thành/xuất)
        boolean completedByInvoiceStatus = hoaDon.getTrangThaiHoaDon() != null && hoaDon.getTrangThaiHoaDon().equals(1);

        return paidByPaymentStatus || completedByInvoiceStatus;
    }

    private void applyTableStateFromInvoice(Ban ban, HoaDon hoaDon) {
        if (ban == null) {
            return;
        }

        if (isInvoicePaid(hoaDon)) {
            ban.setTrangThai(TrangThaiBan.TRONG); // Chuyển bàn về TRỐNG
        } else {
            ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG); // Giữ bàn ĐANG SỬ DỤNG
        }
    }

    private void completeDatBanAndFreeTables(DatBan datBan) {
        if (datBan == null) {
            return;
        }

        // Khi hóa đơn hoàn thành / thanh toán với đặt bàn, cập nhật đặt bàn thành HOAN_THANH
        if (datBan.getTrangThai() == null || datBan.getTrangThai() != TrangThaiDatBan.HOAN_THANH) {
            datBan.setTrangThai(TrangThaiDatBan.HOAN_THANH);
        }

        if (datBan.getSoTienCoc() != null && datBan.getSoTienCoc().compareTo(BigDecimal.ZERO) > 0) {
            datBan.setSoTienCoc(BigDecimal.ZERO);
        }

        // Giải phóng toàn bộ bàn đã liên quan đến DatBan này
        if (datBan.getChiTietDatBanBans() != null) {
            datBan.getChiTietDatBanBans().forEach(ct -> {
                Ban relatedBan = ct.getBan();
                if (relatedBan != null && relatedBan.getTrangThai() != TrangThaiBan.TRONG) {
                    relatedBan.setTrangThai(TrangThaiBan.TRONG);
                    banRepository.save(relatedBan);
                }
            });
        }

        datBanRepository.save(datBan);
    }

    private BigDecimal applyGiamGiaDiscount(BigDecimal tienTruocGiam, GiamGia giamGia) {
        if (tienTruocGiam == null || giamGia == null) {
            return BigDecimal.ZERO;
        }

        String normalizedType = normalizeDiscountType(giamGia.getLoaiGiam());

        if ("PERCENT".equals(normalizedType)) {
            BigDecimal percent = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            BigDecimal discount = tienTruocGiam.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (giamGia.getGiaTriGiamToiDa() != null) {
                discount = discount.min(giamGia.getGiaTriGiamToiDa());
            }
            return discount.min(tienTruocGiam).max(BigDecimal.ZERO);
        }

        if ("FIXED".equals(normalizedType)) {
            BigDecimal fixedDiscount = giamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : giamGia.getGiaTriGiam();
            fixedDiscount = fixedDiscount.min(tienTruocGiam);
            return fixedDiscount.max(BigDecimal.ZERO);
        }

        return BigDecimal.ZERO;
    }

    private void syncInvoiceTotals(HoaDon hd) {
        BigDecimal subtotal = safeMoney(hd.getTienTruocGiam());
        BigDecimal deposit = safeMoney(hd.getTienCoc());
        GiamGia discount = hd.getGiamGia();

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null) {
            validateDiscountCanApply(discount, subtotal);
            discountAmount = applyGiamGiaDiscount(subtotal, discount);
        }

        hd.setTienTruocGiam(subtotal);
        hd.setTienGiamGia(discountAmount);
        hd.setTongTien(subtotal.subtract(discountAmount).subtract(deposit).max(BigDecimal.ZERO));
    }

    private void validateDiscountCanApply(GiamGia giamGia, BigDecimal subtotal) {
        if (giamGia == null) {
            return;
        }

        if (giamGia.getTrangThai() == null || giamGia.getTrangThai() != 1) {
            throw new RuntimeException("Mã giảm giá không hoạt động");
        }

        if (giamGia.getNgayKetThuc() != null && LocalDate.now().isAfter(giamGia.getNgayKetThuc())) {
            giamGia.setTrangThai(0);
            giamGiaRepository.save(giamGia);
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }

        int remainingQuantity = giamGia.getSoLuongMaGiamGia() == null ? 0 : giamGia.getSoLuongMaGiamGia();
        if (remainingQuantity <= 0) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        BigDecimal minimumOrderValue = parseMinimumOrderValue(giamGia.getDieuKienSuDung());
        if (minimumOrderValue != null && subtotal.compareTo(minimumOrderValue) < 0) {
            throw new RuntimeException("Đơn hàng chưa đủ điều kiện sử dụng mã giảm giá");
        }

        if ("UNKNOWN".equals(normalizeDiscountType(giamGia.getLoaiGiam()))) {
            throw new RuntimeException("Loại giảm giá không hợp lệ");
        }
    }

    private BigDecimal parseMinimumOrderValue(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return null;
        }

        String normalized = condition.trim();
        Matcher kMatcher = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*k", Pattern.CASE_INSENSITIVE)
                .matcher(normalized);
        if (kMatcher.find()) {
            return BigDecimal.valueOf(Double.parseDouble(kMatcher.group(1).replace(',', '.')))
                    .multiply(BigDecimal.valueOf(1000));
        }

        String digitsOnly = normalized.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) {
            return null;
        }

        BigDecimal value = new BigDecimal(digitsOnly);
        return value.compareTo(BigDecimal.ZERO) > 0 ? value : null;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.max(BigDecimal.ZERO);
    }

    private void consumeDiscountIfNeeded(InvoiceDiscountState previousState, HoaDon currentInvoice) {
        if (currentInvoice == null || currentInvoice.getGiamGia() == null || !isInvoicePaid(currentInvoice)) {
            return;
        }

        boolean wasAlreadyPaidWithSameDiscount = previousState != null
                && previousState.isPaid()
                && Objects.equals(previousState.discountId(), currentInvoice.getGiamGia().getIdGiamGia());
        if (wasAlreadyPaidWithSameDiscount) {
            return;
        }

        GiamGia discount = currentInvoice.getGiamGia();
        validateDiscountCanApply(discount, safeMoney(currentInvoice.getTienTruocGiam()));
        int remainingQuantity = discount.getSoLuongMaGiamGia() == null ? 0 : discount.getSoLuongMaGiamGia();
        discount.setSoLuongMaGiamGia(Math.max(remainingQuantity - 1, 0));
        discount.setSoLuongDung((discount.getSoLuongDung() == null ? 0 : discount.getSoLuongDung()) + 1);
        if (discount.getSoLuongMaGiamGia() == null || discount.getSoLuongMaGiamGia() <= 0) {
            discount.setTrangThai(0);
        }
        giamGiaRepository.save(discount);
    }

    private record InvoiceDiscountState(Integer invoiceStatus, Integer paymentStatus, Integer discountId) {
        private boolean isPaid() {
            return Objects.equals(paymentStatus, 1) || Objects.equals(invoiceStatus, 1);
        }
    }

    private String normalizeDiscountType(String rawType) {
        if (rawType == null) {
            return "UNKNOWN";
        }

        String normalized = Normalizer.normalize(rawType, Normalizer.Form.NFKD)
                .replaceAll("[\\p{M}]", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();

        if (rawType.contains("%")
                || normalized.contains("PHANTRAM")
                || normalized.contains("PERCENT")
                || (normalized.contains("PHAN") && normalized.contains("TRAM"))
                || (normalized.contains("PH") && normalized.contains("TRAM"))) {
            return "PERCENT";
        }

        if (normalized.contains("TIEN")
                || normalized.contains("MAT")
                || normalized.contains("GIATRI")
                || normalized.contains("GIATR")
                || normalized.contains("VALUE")
                || normalized.contains("VND")
                || normalized.contains("DONG")
                || normalized.contains("FIXED")
                || normalized.contains("CODINH")
                || normalized.contains("CASH")
                || normalized.contains("MONEY")) {
            return "FIXED";
        }

        return "UNKNOWN";
    }

    private LocalDateTime inferEarliestOrderTime(HoaDon hoaDon, List<DTOHoaDonChiTietRequest> chiTietRequests) {
        LocalDateTime fromRequest = chiTietRequests == null ? null : chiTietRequests.stream()
                .map(DTOHoaDonChiTietRequest::getOrderedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (fromRequest != null) {
            return fromRequest;
        }

        if (hoaDon == null || hoaDon.getIdHoaDon() == null) {
            return null;
        }

        return hoaDonChiTietRepository.findByHoaDon_IdHoaDon(hoaDon.getIdHoaDon())
                .stream()
                .map(HoaDonChiTiet::getOrderedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private DTOHoaDonResponse convertToResponse(HoaDon hoaDon) {
        return convertToResponse(hoaDon, null);
    }

    private DTOHoaDonResponse convertToResponse(HoaDon hoaDon, List<DTOHoaDonChiTietRequest> chiTietRequests) {
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
        response.setSoTienChuyenKhoan(hoaDon.getSoTienChuyenKhoan());
        response.setSoTienTienMat(hoaDon.getSoTienTienMat());

        LocalDateTime earliestOrderTime = inferEarliestOrderTime(hoaDon, chiTietRequests);

        if (hoaDon.getBan() != null) {
            response.setIdBan(hoaDon.getBan().getIdBan());
            response.setLoaiBan(hoaDon.getBan().getLoaiBan());
            response.setTenBan(hoaDon.getBan().getTenBan());
        }

        if (hoaDon.getDatBan() != null) {
            response.setIdDatBan(hoaDon.getDatBan().getIdDatBan());
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

        LocalDateTime realGioVao = earliestOrderTime;
        if (realGioVao == null && hoaDon != null && hoaDon.getThoiGianXuat() != null) {
            realGioVao = hoaDon.getThoiGianXuat();
        }
        if (realGioVao == null) {
            realGioVao = LocalDateTime.now();
        }
        response.setGioVaoBan(realGioVao);

        if (hoaDon != null && hoaDon.getThoiGianXuat() != null && Objects.equals(hoaDon.getTrangThaiThanhToan(), 1)) {
            response.setGioRoiBan(hoaDon.getThoiGianXuat());
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
                            dto.setTienGiamGiaMon(ct.getTienGiamGiaMon());
                            dto.setThanhTien(ct.getThanhTien());
                            dto.setOrderedAt(ct.getOrderedAt());
                            dto.setOrderedBy(ct.getOrderedBy());
                            return dto;
                        })
                        .toList()
        );

        return response;
    }
}
