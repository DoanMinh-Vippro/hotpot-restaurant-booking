package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ChiTietGiamGiaComBoValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaComBo;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.DotGiamGia;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaComBoRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietGiamGiaComboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public class ChiTietGiamGiaComboImpl implements ChiTietGiamGiaComboService {
    @Autowired
    private ChiTietGiamGiaComBoRepository repo;

    @Autowired(required = false)
    private ChiTietGiamGiaComBoValidator validator;

    @Autowired
    private DotGiamGiaRepository repo2;

    @Autowired
    private ComboRepository repo3;

    @Override
    public List<ChiTietGiamGiaComboResponse> hienThiCTGGCombo() {
        return repo.hienThiCTGGCombo();
    }

    @Override
    public ChiTietGiamGiaComboResponse detailCTGGCombo(Integer idChiTietGiamGiaCombo) {
        return repo.detailCTGGCombo(idChiTietGiamGiaCombo);
    }

    @Override
    public Page<ChiTietGiamGiaComboResponse> phanTrangCTGGCombo(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return repo.phanTrangCTGGCombo(pageable);
    }

    @Override
    public Page<ChiTietGiamGiaComboResponse> timKiemCTGGCombo(String tenChuongTrinh, String tenCombo, BigDecimal mucMin, BigDecimal mucMax, String loaiGiam,
                                                              Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        String tenChuongTrinhSearch = (tenChuongTrinh != null && !tenChuongTrinh.trim().isEmpty())
                ? "%" + tenChuongTrinh.trim() + "%"
                : null;

        String tenComboSearch = (tenCombo != null && !tenCombo.trim().isEmpty())
                ? "%" + tenCombo.trim() + "%"
                : null;
        String loaiGiamSearch = (loaiGiam != null) ? loaiGiam.trim() : null;
        return repo.timKiemCTGGCombo(tenChuongTrinhSearch, tenComboSearch, mucMin, mucMax, loaiGiamSearch, pageable);
    }

    @Override
    public void addCTGGCombo(ChiTietGiamGiaComboRequest req) {
        if (validator != null) {
            validator.validateAdd(req);
        }
        ChiTietGiamGiaComBo ctggcb = new ChiTietGiamGiaComBo();
        BeanUtils.copyProperties(req, ctggcb);

        DotGiamGia dgg = repo2.findByIdDotGiamGia(req.getIdDotGiamGia());
        ctggcb.setDotGiamGia(dgg);

        Combo c = repo3.findByIdCombo(req.getIdCombo());
        ctggcb.setCombo(c);

        repo.save(ctggcb);
    }

    @Override
    public void updateCTGGCombo(Integer idChiTietGiamGiaCombo, ChiTietGiamGiaComboRequest req) {
        if (validator != null) {
            validator.validateUpdate(idChiTietGiamGiaCombo, req);
        }
        ChiTietGiamGiaComBo ctggcb = repo.findById(idChiTietGiamGiaCombo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết giảm giá combo có id này"));

        DotGiamGia dgg = repo2.findByIdDotGiamGia(req.getIdDotGiamGia());
        ctggcb.setDotGiamGia(dgg);

        Combo c = repo3.findByIdCombo(req.getIdCombo());
        ctggcb.setCombo(c);

        ctggcb.setMucGiam(req.getMucGiam());
        ctggcb.setLoaiGiam(req.getLoaiGiam());
        ctggcb.setTrangThai(req.getTrangThai());

        repo.save(ctggcb);
    }
}
