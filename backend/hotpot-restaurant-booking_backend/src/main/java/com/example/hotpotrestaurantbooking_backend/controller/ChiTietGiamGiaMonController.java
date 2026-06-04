package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietGiamGiaMonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
public class ChiTietGiamGiaMonController {
    @Autowired
    private ChiTietGiamGiaMonService sv;
    @GetMapping("hienThiCTGGM")
    public List<ChiTietGiamGiaMonResponse> hienThiCTGGM(){
        return sv.hienThiCTGGM();
    }
    @GetMapping("detailCTGGM")
    public ChiTietGiamGiaMonResponse detailCTGGM(@RequestParam("idChiTietGiamGiaMon") Integer idChiTietGiamGiaMon){
        return sv.detailCTGGM(idChiTietGiamGiaMon);
    }
    @GetMapping("phanTrangCTGGM")
    public Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(@RequestParam(defaultValue = "0") Integer pageNo,
                                                          @RequestParam(defaultValue = "5") Integer pageSize){

        return sv.phanTrangCTGGM(pageNo, pageSize);
    }
    @GetMapping("timKiemCTGGM")
    public Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(@RequestParam(required = false) String tenChuongTrinh,
                                                        @RequestParam(required = false) String tenMon,
                                                        @RequestParam(required = false) BigDecimal mucMin,
                                                        @RequestParam(required = false) BigDecimal mucMax,
                                                        @RequestParam(defaultValue = "0") Integer pageNo,
                                                        @RequestParam(defaultValue = "5") Integer pageSize){

        return sv.timKiemCTGGM(tenChuongTrinh, tenMon,mucMin, mucMax, pageNo, pageSize);
    }
    @PostMapping("addCTGGM")
    public ResponseEntity<ApiResponse> addCTGGM(@Valid @RequestBody ChiTietGiamGiaMonRequest req){
        sv.addCTGGM(req);
        return ResponseEntity.ok(new ApiResponse("Thêm chi tiết giảm giá món thành công"));
    }
    @PutMapping("updateCTGGM")
    public ResponseEntity<ApiResponse> updateCTGGM(@RequestParam("idChiTietGiamGiaMon") Integer idChiTietGiamGiaMon,
                                                   @Valid @RequestBody ChiTietGiamGiaMonRequest req){
        sv.updateCTGGM(idChiTietGiamGiaMon ,req);
        return ResponseEntity.ok(new ApiResponse("Update chi tiết giảm giá món thành công"));
    }
}
