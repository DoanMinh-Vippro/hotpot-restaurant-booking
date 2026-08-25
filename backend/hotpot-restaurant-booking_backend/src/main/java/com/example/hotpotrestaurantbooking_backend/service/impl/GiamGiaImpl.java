package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.Validation.GiamGiaValidator;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaDTO;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaRequest;
import com.example.hotpotrestaurantbooking_backend.entity.GiamGia;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.GiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.service.GiamGiaService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class GiamGiaImpl implements GiamGiaService {

    @Autowired
    private GiamGiaRepository repo;

    @Autowired
    private GiamGiaValidator validator;

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<GiamGiaDTO> getAll(String keyword) {
        List<GiamGia> entities;
        if (keyword == null || keyword.trim().isEmpty()) {
            entities = repo.findAll();
        } else {
            Page<GiamGia> page = repo.findByMaGiamGiaContainingIgnoreCaseOrDieuKienSuDungContainingIgnoreCase(keyword, keyword, Pageable.unpaged());
            entities = page.getContent();
        }

        autoDisableExpiredDiscounts(entities);
        return entities.stream().map(this::toDto).toList();
    }

    @Override
    public GiamGiaDTO getById(Integer idGiamGia) {
        GiamGia entity = repo.findById(idGiamGia)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy mã giảm giá"));

        if (shouldDisableDueToExpiry(entity)) {
            entity.setTrangThai(0);
            repo.save(entity);
        }

        return toDto(entity);
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

        boolean isNotExpired = request.getNgayKetThuc() != null && !request.getNgayKetThuc().isBefore(LocalDate.now());
        if (isNotExpired) {
            existing.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : 1);
        } else {
            existing.setTrangThai(0);
        }

        if (request.getSoLuongDung() != null) {
            existing.setSoLuongDung(request.getSoLuongDung());
        }

        repo.save(existing);
    }

    @Override
    @Transactional
    public void deleteGiamGia(Integer idGiamGia) {
        GiamGia existing = repo.findById(idGiamGia)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy mã giảm giá"));

        existing.setTrangThai(0);

        try {
            repo.save(existing);
            if (entityManager != null) {
                entityManager.flush();
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Mã giảm giá này đã được sử dụng trong hóa đơn nên không thể xóa vĩnh viễn. Hệ thống đã tự động chuyển về trạng thái ngừng hoạt động", ex);
        }
    }

    @Override
    public List<GiamGiaDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll(null);
        }

        Page<GiamGia> page = repo.findByMaGiamGiaContainingIgnoreCaseOrDieuKienSuDungContainingIgnoreCase(keyword, keyword, Pageable.unpaged());
        List<GiamGia> entities = page.getContent();
        autoDisableExpiredDiscounts(entities);
        return entities.stream().map(this::toDto).toList();
    }

    @Scheduled(fixedDelay = 60000)
    public void autoUpdateExpiredDiscountsTask() {
        List<GiamGia> activeDiscounts = repo.findAll().stream()
                .filter(g -> g != null && g.getTrangThai() != null && g.getTrangThai() == 1)
                .toList();
        autoDisableExpiredDiscounts(activeDiscounts);
    }

    private void autoDisableExpiredDiscounts(List<GiamGia> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        for (GiamGia entity : entities) {
            if (shouldDisableDueToExpiry(entity)) {
                entity.setTrangThai(0);
                repo.save(entity);
            }
        }
    }

    private boolean shouldDisableDueToExpiry(GiamGia entity) {
        return entity != null
                && entity.getTrangThai() != null
                && entity.getTrangThai() == 1
                && entity.getNgayKetThuc() != null
                && isExpiredOnDateOnly(entity.getNgayKetThuc());
    }

    private boolean isExpiredOnDateOnly(LocalDate endDate) {
        return endDate != null && LocalDate.now().isAfter(endDate);
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
