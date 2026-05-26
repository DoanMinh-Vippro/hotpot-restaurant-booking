// ===========================================
// ChucVuService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;

import java.util.List;

public interface ChucVuService {

    List<ChucVu> getAll();

    ChucVu getById(Integer id);

    ChucVu add(ChucVu chucVu);

    ChucVu update(Integer id, ChucVu chucVu);

    void delete(Integer id);
}