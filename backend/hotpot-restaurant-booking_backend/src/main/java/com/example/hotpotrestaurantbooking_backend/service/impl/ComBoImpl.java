package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ComboValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.service.ComBoService;
import com.example.hotpotrestaurantbooking_backend.service.TinhTienService;
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
    @Autowired
    private TinhTienService tienService;

    @Override
    public List<ComboResponse> hienThiComBo(){
        List<ComboResponse> list = repo.hienThiComBo();
        list.forEach(tienService::ganThongTinGiamGiaCombo);
        return list;
    }
    @Override
    public ComboResponse detailComBo(String tenCombo){
        ComboResponse response = repo.detailComBo(tenCombo);
        if (response != null) {
            tienService.ganThongTinGiamGiaCombo(response);
        }
        return response;
    }
    @Override
    public Page<ComboResponse> phanTrangComBo(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ComboResponse> page = repo.phanTrangComBo(pageable);
        page.getContent().forEach(tienService::ganThongTinGiamGiaCombo);
        return page;
    }
    @Override
    public Page<ComboResponse>timKiemComBo(String tenCombo, BigDecimal giaMin, BigDecimal giaMax, Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        // 🛠️ XỬ LÝ NỐI CHUỖI TÌM KIẾM Ở TẦNG JAVA ĐỂ GIỮ NGUYÊN VẸN ĐỊNH DẠNG NVARCHAR
        String tenComboSearch = (tenCombo != null && !tenCombo.trim().isEmpty())
                ? "%" + tenCombo.trim() + "%"
                : null;

        Page<ComboResponse> page = repo.timKiemComBo(
                tenComboSearch, // Truyền chuỗi đã bọc sẵn %
                giaMin,
                giaMax,
                pageable
        );

        page.getContent().forEach(tienService::ganThongTinGiamGiaCombo);
        return page;
    }
    @Override
    public void addComBo(ComboRequest req){
        comboValidator.validateCreate(req);
        Combo cb=new Combo();
        BeanUtils.copyProperties(req, cb);
        if (req.getTrangThai() == 0) {
            cb.setTrangThaiBan(0);
        }
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
        cb.setTrangThaiBan(req.getTrangThaiBan());
        if (req.getTrangThai() == 0) {
            cb.setTrangThaiBan(0);
        } else {
            cb.setTrangThaiBan(req.getTrangThaiBan());
        }
        repo.save(cb);
    }
    @Override
    public void deleteComBo(Integer idCombo){
        Combo cb= repo.findById(idCombo)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy ComBo"));
        cb.setTrangThai(0);
        cb.setTrangThaiBan(0);
        repo.save(cb);
    }
}
