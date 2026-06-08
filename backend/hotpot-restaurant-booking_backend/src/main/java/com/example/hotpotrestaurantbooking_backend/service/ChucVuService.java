package com.example.hotpotrestaurantbooking_backend.service;


import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuResponse;

import java.util.List;

public interface ChucVuService {
    List<DTOChucVuResponse> getAll();

    DTOChucVuResponse findById(Integer id);

    DTOChucVuResponse add(DTOChucVuRequest request);

    DTOChucVuResponse update(Integer id, DTOChucVuRequest request);

    void delete(Integer id);
}
