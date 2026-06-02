package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ChiTietComBoValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComboRequest;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietCombo;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietComBoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public class ChiTietComBoImpl implements ChiTietComBoService {
    @Autowired
    private ChiTietComboRepository repo;
    @Autowired
    private ComboRepository repo2;
    @Autowired
    private MonRepository repo3;
    @Autowired
    private ChiTietComBoValidator validator;

    @Override
    public List<ChiTietComBoResponse> hienThi(){
        return repo.hienThi();
    }
    @Override
    public ChiTietComBoResponse detailCTCB(Integer idChiTietCombo){
        return repo.detailChiTietComBo(idChiTietCombo);
    }
    @Override
    public Page<ChiTietComBoResponse> phanTrangCTCB(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.phanTrangCTComBo(pageable);
    }
    @Override
    public Page<ChiTietComBoResponse>timKiemCTCB(String tenCombo, String tenMon, BigDecimal giaMin, BigDecimal giaMax, Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.timKiemCTCB(tenCombo, tenMon, giaMin, giaMax, pageable);
    }
    @Override
    public void addCTCB(ChiTietComboRequest req){
        validator.validateAdd(req);
        ChiTietCombo ctcb=new ChiTietCombo();
        BeanUtils.copyProperties(req, ctcb);
        Combo cb = repo2.findById(req.getIdCombo())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Combo"));
        ctcb.setCombo(cb);
        Mon m = repo3.findById(req.getIdMon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Món"));
        ctcb.setMon(m);
        repo.save(ctcb);
    }
    @Override
    public void updateCTCB(Integer idChiTietCombo,
                           ChiTietComboRequest req){
        validator.validateUpdate(idChiTietCombo,req);
        ChiTietCombo ctcb=repo.findById(idChiTietCombo)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy thông tin chi tiết của combo này"));
        ctcb.setSoLuong(req.getSoLuong());
        Combo cb = repo2.findById(req.getIdCombo())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Combo"));
        ctcb.setCombo(cb);
        Mon m = repo3.findById(req.getIdMon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Món"));
        ctcb.setMon(m);
        ctcb.setMoTa(req.getMoTa());
        repo.save(ctcb);
    }
}
