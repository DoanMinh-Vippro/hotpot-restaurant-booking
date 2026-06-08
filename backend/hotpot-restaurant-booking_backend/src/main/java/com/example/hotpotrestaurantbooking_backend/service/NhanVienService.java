package com.example.hotpotrestaurantbooking_backend.service;

import org.example.datlich.dto.DTONhanVienRequest;
import org.example.datlich.dto.DTONhanVienResponse;


import java.util.List;

public interface NhanVienService {
    List<DTONhanVienResponse> getAll();

    DTONhanVienResponse findById(Integer id);

    DTONhanVienResponse add(DTONhanVienRequest request);

    DTONhanVienResponse update(Integer id, DTONhanVienRequest request);

    void delete(Integer id);
}
