package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.Validation.DanhMucValidator;
import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResquest;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import com.example.hotpotrestaurantbooking_backend.repository.DanhMucRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanhMucService {
    @Autowired
    private DanhMucRepository repo;
    @Autowired
    private DanhMucValidator validator;

    public List<DanhMucResponse> hienThiDM(){
        return repo.hienThi();
    }
    public DanhMucResponse detailDM(String loaiDanhMuc){
        return repo.deatilDM(loaiDanhMuc);
    }
    public Page<DanhMucResponse> phanTrangDM(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.phanTrangDanhMuc(pageable);
    }
    public Page<DanhMucResponse> timKiemDM(String loaiDanhMuc, Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.timKiemDanhMuc(loaiDanhMuc, pageable);
    }

    public void addDM(DanhMucResquest req){
        validator.validateAdd(req);
        DanhMuc dm=new DanhMuc();
        BeanUtils.copyProperties(req,dm);
        repo.save(dm);
    }
    public void updateDM(Integer idDanhMuc,DanhMucResquest req){
        validator.validateUpdate(idDanhMuc, req);
        DanhMuc dm= repo.findById(idDanhMuc)
                .orElseThrow(()->new RuntimeException("Không tìm thấy danh mục có id này"));
        dm.setLoaiDanhMuc(req.getLoaiDanhMuc());
        dm.setMoTa(req.getMoTa());
        repo.save(dm);
    }
}
