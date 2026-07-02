package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
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
    private KhachHangRepository khachHangRepository;

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
}
