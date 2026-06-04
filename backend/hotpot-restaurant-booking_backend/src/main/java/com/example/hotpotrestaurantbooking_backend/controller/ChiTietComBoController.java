package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComboRequest;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietComBoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
public class ChiTietComBoController {
    @Autowired
    private ChiTietComBoService sv;

    @GetMapping("hienThiCTCB")
    public List<ChiTietComBoResponse>hienThi(){
        return sv.hienThi();
    }

    @GetMapping("detailCTCB")
    public ChiTietComBoResponse detailCTCB(@RequestParam("idChiTietCombo") Integer idChiTietCombo){
        return sv.detailCTCB(idChiTietCombo);
    }
    @GetMapping("phanTrangCTCB")
    public Page<ChiTietComBoResponse> phanTrangCTCB(@RequestParam(defaultValue = "0") Integer pageNo,
                                                    @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.phanTrangCTCB(pageNo,pageSize);
    }
    @GetMapping("timKiemCTCB")
    public Page<ChiTietComBoResponse>timKiemCTCB(@RequestParam(required = false) String tenCombo,
                                                 @RequestParam(required = false) String tenMon,
                                                 @RequestParam(required = false) BigDecimal giaMin,
                                                 @RequestParam(required = false) BigDecimal giaMax,
                                                 @RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.timKiemCTCB(tenCombo, tenMon, giaMin, giaMax, pageNo,pageSize);
    }
    @PostMapping("addCTCB")
    public ResponseEntity<ApiResponse>addCTCB(@Valid @RequestBody ChiTietComboRequest req){
        sv.addCTCB(req);
        return ResponseEntity.ok(new ApiResponse("Thêm ComBo thành công"));
    }
    @PutMapping("updateCTCB")
    public ResponseEntity<ApiResponse>updateCTCB(@RequestParam("idChiTietCombo") Integer idChiTietCombo,
                                                 @Valid @RequestBody ChiTietComboRequest req){
        sv.updateCTCB(idChiTietCombo,req);
        return ResponseEntity.ok(new ApiResponse("Update ComBo thành công"));
    }
}
