package com.example.hotpotrestaurantbooking_backend.service.impl;


import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChucVuServiceImpl implements ChucVuService {
    @Autowired
    private ChucVuRepository repository;

    private ChucVu toEntity(DTOChucVuRequest req){
        return ChucVu.builder()
                .ma_chuc_vu(req.getMaChucVu())
                .ten_chuc_vu(req.getTenChucVu())
                .build();
    }

    private DTOChucVuResponse toResponse(ChucVu cv){
        return new DTOChucVuResponse(
                cv.getId(),
                cv.getMa_chuc_vu(),
                cv.getTen_chuc_vu()
        );
    }

    @Override
    public List<DTOChucVuResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DTOChucVuResponse findById(Integer id) {
        ChucVu cv = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));
        return toResponse(cv);
    }

    @Override
    public DTOChucVuResponse add(DTOChucVuRequest request) {
        return toResponse(repository.save(toEntity(request)));
    }

    @Override
    public DTOChucVuResponse update(Integer id, DTOChucVuRequest request) {
        ChucVu old = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));

        old.setMa_chuc_vu(request.getMaChucVu());
        old.setTen_chuc_vu(request.getTenChucVu());

        return toResponse(repository.save(old));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
