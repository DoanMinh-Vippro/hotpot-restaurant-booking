package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.CocResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CocService {
    List<CocResponse> findAll();
    List<CocResponse> findByTrangThaiCoc(Integer trangThaiCoc);
    List<CocResponse> findByKhachHang(Integer khachHangId);
    BigDecimal tongTienCocDaThu();
}