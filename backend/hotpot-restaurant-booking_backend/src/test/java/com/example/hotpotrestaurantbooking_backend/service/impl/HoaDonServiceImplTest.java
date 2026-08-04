package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoaDonServiceImplTest {

    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private BanRepository banRepository;
    @Mock
    private DatBanRepository datBanRepository;
    @Mock
    private GiamGiaRepository giamGiaRepository;
    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private HoaDonValidator hoaDonValidator;
    @Mock
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @InjectMocks
    private HoaDonServiceImpl hoaDonService;

    @Test
    void addShouldPopulateCustomerAndDepositFromDatBanWhenInvoiceHasNoDirectValues() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdDatBan(7);
        request.setIdBan(3);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        Ban ban = new Ban();
        ban.setIdBan(3);

        DatBan datBan = new DatBan();
        datBan.setIdDatBan(7);
        KhachHang khachHang = new KhachHang();
        khachHang.setIdKhachHang(22);
        khachHang.setTenKhachHang("Nguyễn Văn A");
        datBan.setKhachHang(khachHang);
        datBan.setSdtKhachHang("0909090909");
        datBan.setSoTienCoc(BigDecimal.valueOf(100000));

        when(banRepository.findById(3)).thenReturn(Optional.of(ban));
        when(datBanRepository.findById(7)).thenReturn(Optional.of(datBan));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = hoaDonService.add(request);

        assertEquals(22, response.getIdKhachHang());
        assertEquals("Nguyễn Văn A", response.getTenKhachHang());
        assertEquals("0909090909", response.getSdtKhachHang());
        assertEquals(BigDecimal.valueOf(100000), response.getTienCoc());
    }

    @Test
    void addShouldSetTableToEmptyWhenPaymentIsCompleted() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdBan(4);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        Ban ban = new Ban();
        ban.setIdBan(4);
        ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        when(banRepository.findById(4)).thenReturn(Optional.of(ban));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.add(request);

        assertEquals(TrangThaiBan.TRONG, ban.getTrangThai());
    }

    @Test
    void updateShouldPersistTableStatusToEmptyWhenPaymentIsCompleted() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdBan(5);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        Ban ban = new Ban();
        ban.setIdBan(5);
        ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        HoaDon existing = new HoaDon();
        existing.setTrangThaiThanhToan(0);
        existing.setBan(ban);

        when(hoaDonRepository.findById(10)).thenReturn(Optional.of(existing));
        when(banRepository.findById(5)).thenReturn(Optional.of(ban));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.update(10, request);

        assertEquals(TrangThaiBan.TRONG, ban.getTrangThai());
        verify(banRepository).save(ban);
    }

    @Test
    void addShouldApplyFixedValueDiscountWhenVoucherTypeIsStoredAsVietnameseText() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdBan(8);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);
        request.setTienTruocGiam(BigDecimal.valueOf(100000));
        request.setIdGiamGia(20);

        Ban ban = new Ban();
        ban.setIdBan(8);

        GiamGia voucher = new GiamGia();
        voucher.setIdGiamGia(20);
        voucher.setLoaiGiam("GIÁ TRỊ");
        voucher.setGiaTriGiam(BigDecimal.valueOf(30000));
        voucher.setGiaTriGiamToiDa(BigDecimal.valueOf(30000));

        when(banRepository.findById(8)).thenReturn(Optional.of(ban));
        when(giamGiaRepository.findById(20)).thenReturn(Optional.of(voucher));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = hoaDonService.add(request);

        assertEquals(BigDecimal.valueOf(30000), response.getTienGiamGia());
        assertEquals(BigDecimal.valueOf(70000), response.getTongTien());
    }

    @Test
    void findByBanAndStatusShouldOnlyReturnPendingInvoiceWhenLookingForOpenOrders() {
        HoaDon pendingInvoice = new HoaDon();
        pendingInvoice.setIdHoaDon(12);
        pendingInvoice.setTrangThaiHoaDon(0);
        pendingInvoice.setTrangThaiThanhToan(0);

        Ban busyTable = new Ban();
        busyTable.setIdBan(3);
        busyTable.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        when(banRepository.findById(3)).thenReturn(Optional.of(busyTable));
        when(hoaDonRepository.findFirstByBan_IdBanAndTrangThaiHoaDonAndTrangThaiThanhToan(3, 0, 0))
            .thenReturn(Optional.of(pendingInvoice));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(12)).thenReturn(Collections.emptyList());

        var response = hoaDonService.findByBanAndStatus(3, 0);

        assertEquals(12, response.getIdHoaDon());
    }

    @Test
    void findByBanAndStatusShouldNotReusePendingInvoiceForAvailableTable() {
        HoaDon pendingInvoice = new HoaDon();
        pendingInvoice.setIdHoaDon(13);
        pendingInvoice.setTrangThaiHoaDon(0);
        pendingInvoice.setTrangThaiThanhToan(0);

        Ban emptyTable = new Ban();
        emptyTable.setIdBan(6);
        emptyTable.setTrangThai(TrangThaiBan.TRONG);

        when(banRepository.findById(6)).thenReturn(Optional.of(emptyTable));

        var response = hoaDonService.findByBanAndStatus(6, 0);

        assertNull(response);
    }
}
