package com.example.hotpotrestaurantbooking_backend.service.impl;


import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChucVuServiceImpl implements ChucVuService {
    @Autowired
    private ChucVuRepository repository;

    private String generateNextMaChucVu() {
        List<ChucVu> allRoles = repository.findAll();
        int maxNumber = 0;

        Pattern pattern = Pattern.compile("CV(\\d+)", Pattern.CASE_INSENSITIVE);
        for (ChucVu role : allRoles) {
            if (role.getMaChucVu() == null) continue;
            Matcher matcher = pattern.matcher(role.getMaChucVu().trim());
            if (matcher.find()) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(matcher.group(1)));
            }
        }

        return String.format("CV%02d", maxNumber + 1);
    }

    private ChucVu toEntity(DTOChucVuRequest req){
        String maChucVu = req.getMaChucVu() != null && !req.getMaChucVu().trim().isEmpty()
                ? req.getMaChucVu().trim()
                : generateNextMaChucVu();

        return ChucVu.builder()
                .maChucVu(maChucVu)
                .tenChucVu(req.getTenChucVu())
                .build();
    }

    private DTOChucVuResponse toResponse(ChucVu cv){
        return new DTOChucVuResponse(
                cv.getIdChucVu(),
                cv.getMaChucVu(),
                cv.getTenChucVu()
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

        old.setMaChucVu(request.getMaChucVu() != null && !request.getMaChucVu().trim().isEmpty()
                ? request.getMaChucVu().trim()
                : old.getMaChucVu());
        old.setTenChucVu(request.getTenChucVu());

        return toResponse(repository.save(old));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
