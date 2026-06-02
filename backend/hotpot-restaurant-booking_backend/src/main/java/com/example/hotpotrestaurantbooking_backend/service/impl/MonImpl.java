package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.MonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.MonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.DanhMucRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.MonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
@RequiredArgsConstructor
public class MonImpl implements MonService {
    @Autowired
    private MonRepository repo;
    @Autowired
    private DanhMucRepository repo2;
    @Autowired
    private final MonValidator monValidator;
    @Override
    public List<MonResponse> hienThiMon(){
        return repo.hienThiMon();
    }
    @Override
    public MonResponse detailMon(String tenMon){
        return repo.detailMon(tenMon);
    }
    @Override
    public Page<MonResponse> phanTrangMon(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return repo.phanTrangMon(pageable);
    }   @Override
    public Page<MonResponse> timKiemMon(String tenMon, BigDecimal giaMin, BigDecimal giaMax, String loaiDanhMuc, Integer pageNo, Integer pageSize ) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return repo.timKiemMon(tenMon, giaMin, giaMax, loaiDanhMuc, pageable);
    }
    @Override
    public void addMon(MonRequest req){
        monValidator.validateAdd(req);
        Mon m=new Mon();
        BeanUtils.copyProperties(req, m);
        DanhMuc dm= repo2.findByIdDanhMuc(req.getIdDanhMuc());
        m.setDanhMuc(dm);
        repo.save(m);
    }
    @Override
    public void updateMon(Integer idMon, MonRequest req){
        monValidator.validateUpdate(idMon, req);
        Mon  m= repo.findById(idMon)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món"));
        m.setTenMon(req.getTenMon());
        m.setDonGiaHienTai(req.getDonGiaHienTai());
        DanhMuc dm= repo2.findByIdDanhMuc(req.getIdDanhMuc());
        m.setDanhMuc(dm);
        m.setTrangThai(req.getTrangThai());
        repo.save(m);
    }
    @Override
    public void deleteMon(Integer idMon){
        Mon m = repo.findById(idMon)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món"));
        m.setTrangThai(1);
        repo.save(m);
    }
}
