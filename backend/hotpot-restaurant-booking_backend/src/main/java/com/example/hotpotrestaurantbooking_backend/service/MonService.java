package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.Mon.MonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.Mon.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.Validation.MonValidator;
import com.example.hotpotrestaurantbooking_backend.repository.DanhMucRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
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
public class MonService {
    @Autowired
    private MonRepository repo;
    @Autowired
    private DanhMucRepository repo2;
    @Autowired
    private final MonValidator monValidator;

    public List<MonResponse>hienThiMon(){
        return repo.hienThiMon();
    }

    public MonResponse detailMon(String tenMon){
        return repo.detailMon(tenMon);
    }

    public Page<MonResponse>phanTrangMon(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return repo.phanTrangMon(pageable);
    }
    public Page<MonResponse> timKiemMon( String tenMon, BigDecimal giaMin, BigDecimal giaMax, String loaiDanhMuc, Integer pageNo, Integer pageSize ) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return repo.timKiemMon(tenMon, giaMin, giaMax, loaiDanhMuc, pageable);
    }

    public void addMon(MonRequest req){
        monValidator.validateAdd(req);
        Mon m=new Mon();
        BeanUtils.copyProperties(req, m);
        DanhMuc dm= repo2.findByIdDanhMuc(req.getIdDanhMuc());
        m.setDanhMuc(dm);
        repo.save(m);
    }

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

    public void deleteMon(Integer idMon){
        Mon m = repo.findById(idMon)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món"));
        m.setTrangThai(1);
        repo.save(m);
    }
}
