package com.example.hotpotrestaurantbooking_backend.service;
import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienResponse;

import java.util.List;

public interface NhanVienService {
    List<DTONhanVienResponse> getAll();

    DTONhanVienResponse findById(Integer id);

    DTONhanVienResponse add(DTONhanVienRequest request);

    DTONhanVienResponse update(Integer id, DTONhanVienRequest request);

    void delete(Integer id);
}
