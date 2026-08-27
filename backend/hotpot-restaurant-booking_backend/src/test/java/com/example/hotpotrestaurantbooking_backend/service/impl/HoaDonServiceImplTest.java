package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.HoaDonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietDatBanBan;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
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
    void addShouldConsumeReservationDepositWhenInvoiceIsPaid() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdDatBan(7);
        request.setIdBan(3);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        Ban ban = new Ban();
        ban.setIdBan(3);

        DatBan datBan = new DatBan();
        datBan.setIdDatBan(7);
        datBan.setSoTienCoc(BigDecimal.valueOf(150000));
        datBan.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);

        when(banRepository.findById(3)).thenReturn(Optional.of(ban));
        when(datBanRepository.findById(7)).thenReturn(Optional.of(datBan));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.add(request);

        assertEquals(BigDecimal.ZERO, datBan.getSoTienCoc());
        assertEquals(TrangThaiDatBanCoc.DA_COC, datBan.getTrangThaiCoc());
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
    void addShouldConsumeVoucherAndDisableItWhenLastQuantityIsUsed() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdGiamGia(9);
        request.setTienTruocGiam(BigDecimal.valueOf(100000));
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        GiamGia voucher = new GiamGia();
        voucher.setIdGiamGia(9);
        voucher.setSoLuongMaGiamGia(1);
        voucher.setSoLuongDung(0);
        voucher.setTrangThai(1);
        voucher.setLoaiGiam("FIXED");
        voucher.setGiaTriGiam(BigDecimal.valueOf(10000));

        when(giamGiaRepository.findById(9)).thenReturn(Optional.of(voucher));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.add(request);

        assertEquals(0, voucher.getSoLuongMaGiamGia());
        assertEquals(1, voucher.getSoLuongDung());
        assertEquals(0, voucher.getTrangThai());
        verify(giamGiaRepository).save(voucher);
    }

    @Test
    void addShouldCompleteLinkedReservationAndFreeAllTablesWhenPaid() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdDatBan(7);
        request.setIdBan(4);
        request.setTrangThaiHoaDon(1);
        request.setTrangThaiThanhToan(1);

        Ban ban = new Ban();
        ban.setIdBan(4);
        ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        Ban linkedBan = new Ban();
        linkedBan.setIdBan(5);
        linkedBan.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        DatBan datBan = new DatBan();
        datBan.setIdDatBan(7);
        datBan.setTrangThai(TrangThaiDatBan.DA_NHAN_BAN);
        datBan.setChiTietDatBanBans(Collections.singletonList(new ChiTietDatBanBan() {{
            setBan(linkedBan);
        }}));

        when(banRepository.findById(4)).thenReturn(Optional.of(ban));
        when(datBanRepository.findById(7)).thenReturn(Optional.of(datBan));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.add(request);

        assertEquals(TrangThaiDatBan.HOAN_THANH, datBan.getTrangThai());
        assertEquals(TrangThaiBan.TRONG, linkedBan.getTrangThai());
    }

    @Test
    void updateShouldNotCompleteLinkedReservationWhenInvoiceStillPending() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setIdDatBan(7);
        request.setIdBan(4);
        request.setTrangThaiHoaDon(0);
        request.setTrangThaiThanhToan(0);

        Ban ban = new Ban();
        ban.setIdBan(4);
        ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        Ban linkedBan = new Ban();
        linkedBan.setIdBan(5);
        linkedBan.setTrangThai(TrangThaiBan.DANG_SU_DUNG);

        DatBan datBan = new DatBan();
        datBan.setIdDatBan(7);
        datBan.setTrangThai(TrangThaiDatBan.DA_NHAN_BAN);
        datBan.setChiTietDatBanBans(Collections.singletonList(new ChiTietDatBanBan() {{
            setBan(linkedBan);
        }}));

        HoaDon existingHoaDon = new HoaDon();
        existingHoaDon.setIdHoaDon(1);
        existingHoaDon.setBan(ban);
        existingHoaDon.setDatBan(datBan);

        when(hoaDonRepository.findById(1)).thenReturn(Optional.of(existingHoaDon));
        when(banRepository.findById(4)).thenReturn(Optional.of(ban));
        when(datBanRepository.findById(7)).thenReturn(Optional.of(datBan));
        when(hoaDonChiTietRepository.findByHoaDon_IdHoaDon(any())).thenReturn(Collections.emptyList());
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hoaDonService.update(1, request);

        assertEquals(TrangThaiDatBan.DA_NHAN_BAN, datBan.getTrangThai());
        assertEquals(TrangThaiBan.DANG_SU_DUNG, linkedBan.getTrangThai());
    }
}
