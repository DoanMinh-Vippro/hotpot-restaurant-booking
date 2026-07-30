package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.*;

import java.util.List;

public interface OrderSevice {
    List<DTOKhuVucOrderResponse> getDanhSachBanOrder();
    DTOOrderHoaDonResponse chonBan(Integer idBan);
    DTOOrderMenuResponse getMenu();
    void themMon(DTOOrderThemMonRequest request);
    void themCombo(DTOOrderThemComboRequest request);
    DTOOrderHoaDonChiTietResponse getChiTietHoaDon(Integer idHoaDon);
}
