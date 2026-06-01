package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.Validation.DotGiamGiaValidator;
import com.example.hotpotrestaurantbooking_backend.Validation.MonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DotGiamGia;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepository repo;
    @Autowired
    private DotGiamGiaValidator validator;

    public List<DotGiamGiaResponse>hienThiDGG(){
        return repo.hienThiDGG();
    }
    public DotGiamGiaResponse detailDGG(String tenChuongTrinh){
        return repo.detailDGG(tenChuongTrinh);
    }
    public Page<DotGiamGiaResponse> phanTrangDGG(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.phanTrangDGG(pageable);
    }
    public Page<DotGiamGiaResponse> timKiemDGG(String tenChuongTrinh, LocalDate tuNgay, LocalDate denNgay, Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.timKiemDGG(tenChuongTrinh, tuNgay, denNgay, pageable);
    }

    public void addDGG(DotGiamGiaRequest req){
        validator.validateAdd(req);
        DotGiamGia dgg=new DotGiamGia();
        BeanUtils.copyProperties(req,dgg);
        repo.save(dgg);
    }
    public void updateDGG(Integer idDotGiamGia, DotGiamGiaRequest req){
        validator.validateUpdate(idDotGiamGia, req);
        DotGiamGia dgg= repo.findById(idDotGiamGia)
                .orElseThrow(()->new RuntimeException("Không tìm thấy đợt giảm giá"));
        dgg.setTenChuongTrinh(req.getTenChuongTrinh());
        dgg.setNgayBatDau(req.getNgayBatDau());
        dgg.setNgayKetThuc(req.getNgayKetThuc());
        repo.save(dgg);
    }
}
