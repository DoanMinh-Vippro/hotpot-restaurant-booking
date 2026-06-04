package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.GiamGiaValidator;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaDTO;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaRequest;
import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.GiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.service.GiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GiamGiaImpl implements GiamGiaService {

    @Autowired
    private GiamGiaRepository repo;

    @Autowired
    private GiamGiaValidator validator;

    @Override
    public List<GiamGiaDTO> getAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.findAll().stream().map(this::toDto).toList();
        }
        return repo.findByMaGiamGiaContainingIgnoreCaseOrDieuKienSuDungContainingIgnoreCase(keyword, keyword, Pageable.unpaged())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public GiamGiaDTO getById(Integer idGiamGia) {
        return repo.findById(idGiamGia)
                .map(this::toDto)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy mã giảm giá"));
    }

    @Override
    public void createGiamGia(GiamGiaRequest request) {
        validator.validateAdd(request);

        if (repo.existsByMaGiamGia(request.getMaGiamGia())) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }

        GiamGia entity = new GiamGia();
        entity.setMaGiamGia(request.getMaGiamGia().trim());
        entity.setNgayTao(LocalDate.now());
        entity.setNgayKetThuc(request.getNgayKetThuc());
        entity.setDieuKienSuDung(request.getDieuKienSuDung().trim());
        entity.setGiaTriGiamToiDa(request.getGiaTriGiamToiDa());
        entity.setGiaTriGiam(request.getGiaTriGiam());
        entity.setLoaiGiam(request.getLoaiGiam().trim());
        entity.setSoLuongMaGiamGia(request.getSoLuongMaGiamGia());
        entity.setSoLuongDung(0);
        entity.setTrangThai(request.getTrangThai());

        repo.save(entity);
    }

    @Override
    public void updateGiamGia(Integer idGiamGia, GiamGiaRequest request) {
        GiamGia existing = repo.findById(idGiamGia)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy mã giảm giá"));

        validator.validateUpdate(idGiamGia, request, existing.getNgayTao());

        if (!existing.getMaGiamGia().equalsIgnoreCase(request.getMaGiamGia())
                && repo.existsByMaGiamGia(request.getMaGiamGia())) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }

        existing.setMaGiamGia(request.getMaGiamGia().trim());
        existing.setNgayKetThuc(request.getNgayKetThuc());
        existing.setDieuKienSuDung(request.getDieuKienSuDung().trim());
        existing.setGiaTriGiamToiDa(request.getGiaTriGiamToiDa());
        existing.setGiaTriGiam(request.getGiaTriGiam());
        existing.setLoaiGiam(request.getLoaiGiam().trim());
        existing.setSoLuongMaGiamGia(request.getSoLuongMaGiamGia());
        existing.setTrangThai(request.getTrangThai());

        if (request.getSoLuongDung() != null) {
            existing.setSoLuongDung(request.getSoLuongDung());
        }

        repo.save(existing);
    }

    @Override
    public void deleteGiamGia(Integer idGiamGia) {
        if (!repo.existsById(idGiamGia)) {
            throw new CustomResourceNotFoundException("Không tìm thấy mã giảm giá");
        }
        repo.deleteById(idGiamGia);
    }

    private GiamGiaDTO toDto(GiamGia entity) {
        return GiamGiaDTO.builder()
                .idGiamGia(entity.getIdGiamGia())
                .maGiamGia(entity.getMaGiamGia())
                .ngayTao(entity.getNgayTao())
                .ngayKetThuc(entity.getNgayKetThuc())
                .dieuKienSuDung(entity.getDieuKienSuDung())
                .giaTriGiamToiDa(entity.getGiaTriGiamToiDa())
                .giaTriGiam(entity.getGiaTriGiam())
                .loaiGiam(entity.getLoaiGiam())
                .soLuongMaGiamGia(entity.getSoLuongMaGiamGia())
                .soLuongDung(entity.getSoLuongDung())
                .trangThai(entity.getTrangThai())
                .build();
    }
}
