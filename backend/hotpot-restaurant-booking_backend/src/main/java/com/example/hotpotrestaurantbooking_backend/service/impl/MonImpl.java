package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.MonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.MonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DanhMuc;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.DanhMucRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.MonService;
import com.example.hotpotrestaurantbooking_backend.service.TinhTienService;
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
    @Autowired
    private final TinhTienService tienService;

    @Override
    public List<MonResponse> hienThiMon() {
        List<MonResponse> list = repo.hienThiMon();
        list.forEach(tienService::ganThongTinGiamGia);
        return list;
    }

    @Override
    public MonResponse detailMon(String tenMon) {
        MonResponse response = repo.detailMon(tenMon);
        tienService.ganThongTinGiamGia(response);
        return response;
    }

    @Override
    public Page<MonResponse> phanTrangMon(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<MonResponse> page = repo.phanTrangMon(pageable);
        page.getContent().forEach(tienService::ganThongTinGiamGia);
        return page;
    }

    @Override
    public Page<MonResponse> timKiemMon(String tenMon, BigDecimal giaMin, BigDecimal giaMax, String loaiDanhMuc, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        // 🛠️ XỬ LÝ NỐI CHUỖI TÌM KIẾM Ở TẦNG JAVA ĐỂ GIỮ NGUYÊN VẸN ĐỊNH DẠNG NVARCHAR
        String tenMonSearch = (tenMon != null && !tenMon.trim().isEmpty())
                ? "%" + tenMon.trim() + "%"
                : null;

        String loaiDanhMucSearch = (loaiDanhMuc != null && !loaiDanhMuc.trim().isEmpty())
                ? "%" + loaiDanhMuc.trim() + "%"
                : null;

        Page<MonResponse> page = repo.timKiemMon(
                tenMonSearch,       // Truyền chuỗi đã bọc sẵn %
                giaMin,
                giaMax,
                loaiDanhMucSearch,  // Truyền chuỗi đã bọc sẵn %
                pageable
        );

        page.getContent().forEach(tienService::ganThongTinGiamGia);
        return page;
    }

    @Override
    public void addMon(MonRequest req) {
        monValidator.validateAdd(req);
        Mon m = new Mon();
        BeanUtils.copyProperties(req, m);
        if (req.getTrangThai() == 1) {
            m.setTrangThaiBan(0);
        }
        DanhMuc dm = repo2.findByIdDanhMuc(req.getIdDanhMuc());
        m.setDanhMuc(dm);
        repo.save(m);
    }

    @Override
    public void updateMon(Integer idMon, MonRequest req) {
        monValidator.validateUpdate(idMon, req);
        Mon m = repo.findById(idMon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món"));
        m.setTenMon(req.getTenMon());
        m.setDonGiaHienTai(req.getDonGiaHienTai());
        DanhMuc dm = repo2.findByIdDanhMuc(req.getIdDanhMuc());
        m.setDanhMuc(dm);
        m.setTrangThai(req.getTrangThai());
        m.setTrangThaiBan(req.getTrangThaiBan());
        if (req.getTrangThai() == 1) {
            m.setTrangThaiBan(0);
        } else {
            m.setTrangThaiBan(req.getTrangThaiBan());
        }
        m.setHinhAnh(req.getHinhAnh());
        repo.save(m);
    }

    @Override
    public void deleteMon(Integer idMon) {
        Mon m = repo.findById(idMon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món"));
        m.setTrangThai(1);
        m.setTrangThaiBan(0);
        repo.save(m);
    }
}