package org.example.datlich.impl;

import org.example.datlich.dto.DTOChucVuRequest;
import org.example.datlich.dto.DTOChucVuResponse;
import org.example.datlich.entity.ChucVu;
import org.example.datlich.repository.ChucVuRepository;
import org.example.datlich.service.ChucVuService;
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
    public DTOChucVuResponse getById(Integer id) {
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
