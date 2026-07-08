package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatBanQuanLyServiceImplTest {

    @Mock
    private ModelMapper mapper;

    @Mock
    private DatBanRepository datBanRepository;

    @Mock
    private BanRepository banRepository;

    @Mock
    private ComboRepository comboRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private HoaDonRepository hoaDonRepository;

    @InjectMocks
    private DatBanQuanLyServiceImpl service;

    @Test
    void updateShouldPersistTrangThaiCocAndCustomerInfo() {
        DatBan datBan = new DatBan();
        datBan.setIdDatBan(1);
        datBan.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);

        KhachHang khachHang = new KhachHang();
        khachHang.setIdKhachHang(10);
        khachHang.setTenKhachHang("Nguyễn Văn A");
        datBan.setKhachHang(khachHang);

        DTODatBanQuanLyRequest request = new DTODatBanQuanLyRequest();
        request.setIdkhachHang(10);
        request.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN);
        request.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);
        request.setSoTienCoc(BigDecimal.TEN);
        request.setSdtKhachHang("0987654321");

        DTODatBanQuanLyResponse response = new DTODatBanQuanLyResponse();
        response.setIdDatBan(1);

        when(datBanRepository.findById(1)).thenReturn(Optional.of(datBan));
        when(khachHangRepository.findById(10)).thenReturn(Optional.of(khachHang));
        when(mapper.map(any(DatBan.class), eq(DTODatBanQuanLyResponse.class))).thenReturn(response);

        service.update(1, request);

        assertEquals(TrangThaiDatBan.DA_XAC_NHAN, datBan.getTrangThai());
        assertEquals(TrangThaiDatBanCoc.DA_COC, datBan.getTrangThaiCoc());
        assertEquals("0987654321", datBan.getSdtKhachHang());
        assertEquals(10, datBan.getKhachHang().getIdKhachHang());
    }

    @Test
    void addShouldDefaultDepositToZeroWhenNotProvided() {
        DatBan datBan = new DatBan();
        datBan.setIdDatBan(2);
        datBan.setSoTienCoc(null);

        DTODatBanQuanLyRequest request = new DTODatBanQuanLyRequest();
        request.setIdkhachHang(10);
        request.setSdtKhachHang("0987654321");
        request.setSoNguoi(4);
        request.setSoTienCoc(null);

        KhachHang khachHang = new KhachHang();
        khachHang.setIdKhachHang(10);
        khachHang.setTenKhachHang("Nguyễn Văn A");

        when(mapper.map(any(DTODatBanQuanLyRequest.class), eq(DatBan.class))).thenReturn(datBan);
        when(khachHangRepository.findById(10)).thenReturn(Optional.of(khachHang));
        when(datBanRepository.save(any(DatBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(any(DatBan.class), eq(DTODatBanQuanLyResponse.class))).thenAnswer(invocation -> {
            DTODatBanQuanLyResponse response = new DTODatBanQuanLyResponse();
            response.setIdDatBan(((DatBan) invocation.getArgument(0)).getIdDatBan());
            return response;
        });

        DTODatBanQuanLyResponse response = service.add(request);

        assertEquals(BigDecimal.ZERO, datBan.getSoTienCoc());
        assertEquals(BigDecimal.ZERO, response.getSoTienCoc());
    }

    @Test
    void addShouldClearTransientBanBeforeSaving() {
        DatBan datBan = new DatBan();
        datBan.setIdDatBan(3);
        Ban transientBan = new Ban();
        transientBan.setIdBan(99);
        datBan.setBan(transientBan);

        DTODatBanQuanLyRequest request = new DTODatBanQuanLyRequest();
        request.setIdkhachHang(null);
        request.setSoNguoi(2);
        request.setSoTienCoc(BigDecimal.ZERO);

        when(mapper.map(any(DTODatBanQuanLyRequest.class), eq(DatBan.class))).thenReturn(datBan);
        when(datBanRepository.save(any(DatBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(any(DatBan.class), eq(DTODatBanQuanLyResponse.class))).thenAnswer(invocation -> {
            DTODatBanQuanLyResponse response = new DTODatBanQuanLyResponse();
            response.setIdDatBan(((DatBan) invocation.getArgument(0)).getIdDatBan());
            return response;
        });

        service.add(request);

        assertEquals(null, datBan.getBan());
    }

    @Test
    void addShouldAttachExistingComboWhenProvided() {
        DatBan datBan = new DatBan();
        datBan.setIdDatBan(4);

        Combo combo = new Combo();
        combo.setIdCombo(7);
        combo.setTenCombo("Combo gia đình");

        DTODatBanQuanLyRequest request = new DTODatBanQuanLyRequest();
        request.setIdCombo(7);
        request.setSoNguoi(4);
        request.setSoTienCoc(BigDecimal.ZERO);

        when(mapper.map(any(DTODatBanQuanLyRequest.class), eq(DatBan.class))).thenReturn(datBan);
        when(comboRepository.findById(7)).thenReturn(Optional.of(combo));
        when(datBanRepository.save(any(DatBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(any(DatBan.class), eq(DTODatBanQuanLyResponse.class))).thenAnswer(invocation -> {
            DTODatBanQuanLyResponse response = new DTODatBanQuanLyResponse();
            response.setIdDatBan(((DatBan) invocation.getArgument(0)).getIdDatBan());
            return response;
        });

        service.add(request);

        assertEquals(7, datBan.getCombo().getIdCombo());
    }
}
