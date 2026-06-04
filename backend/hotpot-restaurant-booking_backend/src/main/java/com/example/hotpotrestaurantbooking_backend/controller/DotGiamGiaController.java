package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DotGiamGiaResponse;
import com.example.hotpotrestaurantbooking_backend.service.DotGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
public class DotGiamGiaController {
    @Autowired
    private DotGiamGiaService sv;

    @GetMapping("hienThiDGG")
    public List<DotGiamGiaResponse> hienThiDGG(){
        return sv.hienThiDGG();
    }
    @GetMapping("detailDGG")
    public DotGiamGiaResponse detailDGG(@RequestParam("tenChuongTrinh") String tenChuongTrinh){
        return sv.detailDGG(tenChuongTrinh);
    }
    @GetMapping("phanTrangDGG")
    public Page<DotGiamGiaResponse> phanTrangDGG(@RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.phanTrangDGG(pageNo,pageSize);
    }
    @GetMapping("timKiemDGG")
    public Page<DotGiamGiaResponse> timKiemDGG(
            @RequestParam(required = false) String tenChuongTrinh,
            @RequestParam(required = false) LocalDate tuNgay,
            @RequestParam(required = false) LocalDate denNgay,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.timKiemDGG(tenChuongTrinh, tuNgay, denNgay, pageNo,pageSize);
    }
    @PostMapping("addDGG")
    public ResponseEntity<ApiResponse>addDGG(@Valid @RequestBody DotGiamGiaRequest req){
        sv.addDGG(req);
        return ResponseEntity.ok(new ApiResponse("Thêm đợt giảm giá thành công"));
    }
    @PutMapping("updateDGG")
    public ResponseEntity<ApiResponse> updateMon(@RequestParam("idDotGiamGia") Integer idDotGiamGia, @Valid @RequestBody DotGiamGiaRequest req){
        sv.updateDGG(idDotGiamGia, req);
        return ResponseEntity.ok(new ApiResponse("Update đợt giảm giá thành công"));
    }
}
