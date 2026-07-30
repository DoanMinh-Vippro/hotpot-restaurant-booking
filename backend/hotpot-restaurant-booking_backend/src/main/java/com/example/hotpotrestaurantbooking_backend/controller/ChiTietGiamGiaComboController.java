package com.example.hotpotrestaurantbooking_backend.controller;
import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaComboResponse;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietGiamGiaComboService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@RestController
public class ChiTietGiamGiaComboController {
    @Autowired
    private ChiTietGiamGiaComboService sv;

    @GetMapping("hienThiCTGGC")
    public List<ChiTietGiamGiaComboResponse> hienThiCTGGCombo() {
        return sv.hienThiCTGGCombo();
    }

    @GetMapping("detailCTGGC")
    public ChiTietGiamGiaComboResponse detailCTGGCombo(@RequestParam("idChiTietGiamGiaCombo") Integer idChiTietGiamGiaCombo) {
        return sv.detailCTGGCombo(idChiTietGiamGiaCombo);
    }

    @GetMapping("phanTrangCTGGC")
    public Page<ChiTietGiamGiaComboResponse> phanTrangCTGGCombo(@RequestParam(defaultValue = "0") Integer pageNo,
                                                                @RequestParam(defaultValue = "5") Integer pageSize) {
        return sv.phanTrangCTGGCombo(pageNo, pageSize);
    }

    @GetMapping("timKiemCTGGC")
    public Page<ChiTietGiamGiaComboResponse> timKiemCTGGCombo(@RequestParam(required = false) String tenChuongTrinh,
                                                              @RequestParam(required = false) String tenCombo,
                                                              @RequestParam(required = false) BigDecimal mucMin,
                                                              @RequestParam(required = false) BigDecimal mucMax,
                                                              @RequestParam(required = false) String loaiGiam,
                                                              @RequestParam(defaultValue = "0") Integer pageNo,
                                                              @RequestParam(defaultValue = "5") Integer pageSize) {
        return sv.timKiemCTGGCombo(tenChuongTrinh, tenCombo, mucMin, mucMax, loaiGiam, pageNo, pageSize);
    }

    @PostMapping("addCTGGC")
    public ResponseEntity<ApiResponse> addCTGGCombo(@Valid @RequestBody ChiTietGiamGiaComboRequest req) {
        sv.addCTGGCombo(req);
        return ResponseEntity.ok(new ApiResponse("Thêm chi tiết giảm giá combo thành công"));
    }

    @PutMapping("updateCTGGC")
    public ResponseEntity<ApiResponse> updateCTGGCombo(@RequestParam("idChiTietGiamGiaCombo") Integer idChiTietGiamGiaCombo,
                                                       @Valid @RequestBody ChiTietGiamGiaComboRequest req) {
        sv.updateCTGGCombo(idChiTietGiamGiaCombo, req);
        return ResponseEntity.ok(new ApiResponse("Update chi tiết giảm giá combo thành công"));
    }
}
