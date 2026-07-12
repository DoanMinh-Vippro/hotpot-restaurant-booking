package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietDatBanComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatBanServiceImplTest {

    @Mock
    private DatBanRepository datBanRepository;

    @Mock
    private ComboRepository comboRepository;

    @Mock
    private ChiTietDatBanComboRepository chiTietDatBanComboRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private DatBanServiceImpl datBanService;

    @Test
    void getAllShouldReturnEmptyComboListWhenComboDetailQueryFails() {
        DatBan datBan = new DatBan();
        datBan.setIdDatBan(1);

        when(datBanRepository.findAll()).thenReturn(List.of(datBan));
        when(mapper.map(datBan, DTODatBanResponse.class)).thenReturn(new DTODatBanResponse());
        when(chiTietDatBanComboRepository.findByDatBan_IdDatBan(1))
                .thenThrow(new DataAccessException("Invalid object name 'ChiTietDatBanCombo'") {});

        var response = datBanService.getAll();

        assertNotNull(response);
        assertTrue(response.get(0).getDsCombo().isEmpty());
    }
}
