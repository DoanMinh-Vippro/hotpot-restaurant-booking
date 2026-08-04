package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.GiamGiaValidator;
import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import com.example.hotpotrestaurantbooking_backend.repository.GiamGiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiamGiaImplTest {

    @Mock
    private GiamGiaRepository repo;

    @Mock
    private GiamGiaValidator validator;

    @InjectMocks
    private GiamGiaImpl service;

    @Test
    void deleteGiamGia_shouldDisableDiscountInsteadOfHardDelete() {
        GiamGia entity = new GiamGia();
        entity.setIdGiamGia(7);
        entity.setTrangThai(1);

        when(repo.findById(7)).thenReturn(Optional.of(entity));

        service.deleteGiamGia(7);

        assertEquals(0, entity.getTrangThai());
        verify(repo).save(entity);
        verify(repo, never()).deleteById(7);
    }

    @Test
    void getAll_shouldDisableExpiredDiscountsAutomatically() {
        GiamGia entity = new GiamGia();
        entity.setIdGiamGia(11);
        entity.setTrangThai(1);
        entity.setNgayKetThuc(LocalDate.now().minusDays(1));

        when(repo.findAll()).thenReturn(List.of(entity));

        service.getAll(null);

        assertEquals(0, entity.getTrangThai());
        verify(repo).save(entity);
    }

    @Test
    void getAll_shouldKeepDiscountActiveOnExpiryDate() {
        GiamGia entity = new GiamGia();
        entity.setIdGiamGia(12);
        entity.setTrangThai(1);
        entity.setNgayKetThuc(LocalDate.now());

        when(repo.findAll()).thenReturn(List.of(entity));

        service.getAll(null);

        assertEquals(1, entity.getTrangThai());
        verify(repo, never()).save(entity);
    }
}
