package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ChiTietGiamGiaMonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.entity.DotGiamGia;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietGiamGiaMonService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ChiTietGiamGiaMonImpl implements ChiTietGiamGiaMonService {
    @Autowired
    private ChiTietGiamGiaMonRepository repo;
    @Autowired
    private ChiTietGiamGiaMonValidator validator;
    @Autowired
    private DotGiamGiaRepository repo2;
    @Autowired
    private MonRepository repo3;

    public List<ChiTietGiamGiaMonResponse> hienThiCTGGM(){
        return repo.hienThiCTGGM();
    }

    public ChiTietGiamGiaMonResponse detailCTGGM(Integer idChiTietGiamGiaMon){
        return repo.detailCTGGM(idChiTietGiamGiaMon);
    }

    public Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return repo.phanTrangCTGGM(pageable);
    }

    public Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(String tenChuongTrinh, String tenMon,BigDecimal mucMin, BigDecimal mucMax,
                                                        Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return repo.timKiemCTGGM(tenChuongTrinh, tenMon,mucMin, mucMax, pageable);
    }

    public void addCTGGM(ChiTietGiamGiaMonRequest req){
        validator.validateAdd(req);
        ChiTietGiamGiaMon ctggm=new ChiTietGiamGiaMon();
        BeanUtils.copyProperties(req, ctggm);
        DotGiamGia dgg= repo2.findByIdDotGiamGia(req.getIdDotGiamGia());
        ctggm.setDotGiamGia(dgg);
        Mon m= repo3.findByIdMon(req.getIdMon());
        ctggm.setMon(m);
        repo.save(ctggm);
    }

    public void updateCTGGM(Integer idChiTietGiamGiaMon,
                            ChiTietGiamGiaMonRequest req){
        validator.validateUpdate(idChiTietGiamGiaMon, req);
        ChiTietGiamGiaMon ctggm=repo.findById(idChiTietGiamGiaMon)
                .orElseThrow(()->new RuntimeException("Không tìm thấy chi tiết giảm giá món có id này"));
        DotGiamGia dgg= repo2.findByIdDotGiamGia(req.getIdDotGiamGia());
        ctggm.setDotGiamGia(dgg);
        Mon m= repo3.findByIdMon(req.getIdMon());
        ctggm.setMon(m);
        ctggm.setMucGiam(req.getMucGiam());
        repo.save(ctggm);
    }
}
