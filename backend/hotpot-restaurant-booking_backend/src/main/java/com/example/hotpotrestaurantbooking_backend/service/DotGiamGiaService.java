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


public interface DotGiamGiaService {
    List<DotGiamGiaResponse> hienThiDGG();

    DotGiamGiaResponse detailDGG(String tenChuongTrinh);

    Page<DotGiamGiaResponse> phanTrangDGG(
            Integer pageNo,
            Integer pageSize
    );

    Page<DotGiamGiaResponse> timKiemDGG(
            String tenChuongTrinh,
            LocalDate tuNgay,
            LocalDate denNgay,
            Integer pageNo,
            Integer pageSize
    );

    void addDGG(DotGiamGiaRequest req);

    void updateDGG(Integer idDotGiamGia,
                   DotGiamGiaRequest req);
}
