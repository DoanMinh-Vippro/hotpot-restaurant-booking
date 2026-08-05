package com.example.hotpotrestaurantbooking_backend.Validation;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.repository.GiamGiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HoaDonValidatorTest {

    @Mock
    private GiamGiaRepository giamGiaRepository;

    private HoaDonValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new HoaDonValidator(giamGiaRepository);
    }

    @Test
    void validateAddShouldRejectCompletedInvoiceWithZeroTotal() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setMaHoaDon("HD001");
        request.setTrangThaiHoaDon(1);
        request.setTongTien(BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> validator.validateAdd(request));
    }

    @Test
    void validateAddShouldAllowCompletedInvoiceWithPositiveTotal() {
        DTOHoaDonRequest request = new DTOHoaDonRequest();
        request.setMaHoaDon("HD002");
        request.setTrangThaiHoaDon(1);
        request.setTongTien(BigDecimal.TEN);
        request.setChiTiet(List.of(new DTOHoaDonChiTietRequest()));
        request.getChiTiet().get(0).setSoLuong(1);
        request.getChiTiet().get(0).setThanhTien(BigDecimal.TEN);

        assertDoesNotThrow(() -> validator.validateAdd(request));
    }
}
