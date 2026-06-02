package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ComboValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.service.ComBoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;


@Service
public class ComBoImpl implements ComBoService {
    @Autowired
    private ComboRepository repo;
    @Autowired
    private ComboValidator comboValidator;

    @Override
    public List<ComboResponse> hienThiComBo(){
        return repo.hienThiComBo();
    }
    @Override
    public ComboResponse detailComBo(String tenCombo){
        return repo.detailComBo(tenCombo);
    }
    @Override
    public Page<ComboResponse> phanTrangComBo(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.phanTrangComBo(pageable);
    }
    @Override
    public Page<ComboResponse>timKiemComBo(String tenCombo, BigDecimal giaMin, BigDecimal giaMax, Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return repo.timKiemComBo(tenCombo, giaMin, giaMax, pageable);
    }
    @Override
    public void addComBo(ComboRequest req){
        comboValidator.validateCreate(req);
        Combo cb=new Combo();
        BeanUtils.copyProperties(req, cb);
        repo.save(cb);
    }
    @Override
    public void updateComBo(Integer idCombo,
                            ComboRequest req){
        comboValidator.validateUpdate(idCombo, req);
        Combo cb= repo.findById(idCombo)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy ComBo"));
        cb.setTenCombo(req.getTenCombo());
        cb.setGiaCombo(req.getGiaCombo());
        cb.setHinhAnh(req.getHinhAnh());
        cb.setTrangThai(req.getTrangThai());
        repo.save(cb);
    }
    @Override
    public void deleteComBo(Integer idCombo){
        Combo cb= repo.findById(idCombo)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy ComBo"));
        cb.setTrangThai(0);
        repo.save(cb);
    }
}
