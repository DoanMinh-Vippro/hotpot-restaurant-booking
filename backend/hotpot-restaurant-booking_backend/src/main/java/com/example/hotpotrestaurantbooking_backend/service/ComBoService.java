package com.example.hotpotrestaurantbooking_backend.service;


import com.example.hotpotrestaurantbooking_backend.dto.ComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.List;


public interface ComBoService {
    List<ComboResponse> hienThiComBo();

    ComboResponse detailComBo(String tenCombo);

    Page<ComboResponse> phanTrangComBo(Integer pageNo, Integer pageSize);

    Page<ComboResponse> timKiemComBo(
            String tenCombo,
            BigDecimal giaMin,
            BigDecimal giaMax,
            Integer pageNo,
            Integer pageSize
    );

    void addComBo(ComboRequest req);

    void updateComBo(Integer idCombo, ComboRequest req);

    void deleteComBo(Integer idCombo);
}
