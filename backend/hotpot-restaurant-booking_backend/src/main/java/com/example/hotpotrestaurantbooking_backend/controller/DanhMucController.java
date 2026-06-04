package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DanhMucResquest;
import com.example.hotpotrestaurantbooking_backend.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class DanhMucController {
    @Autowired
    private DanhMucService sv;

    @GetMapping("hienThiDM")
    public List<DanhMucResponse> hienThiDM(){
        return sv.hienThiDM();
    }
    @GetMapping("detailDM")
    public DanhMucResponse detailDM(@RequestParam("loaiDanhMuc") String loaiDanhMuc){
        return sv.detailDM(loaiDanhMuc);
    }
    @GetMapping("phanTrangDM")
    public Page<DanhMucResponse> phanTrangDM(@RequestParam(defaultValue = "0")Integer pageNo,
                                             @RequestParam(defaultValue = "5")Integer pageSize){
        return sv.phanTrangDM(pageNo, pageSize);
    }
    @GetMapping("timKiemDM")
    public Page<DanhMucResponse> timKiemDM(@RequestParam(required = false) String loaiDanhMuc,
                                           @RequestParam(defaultValue = "0")Integer pageNo,
                                           @RequestParam(defaultValue = "5")Integer pageSize){
        return sv.timKiemDM(loaiDanhMuc, pageNo, pageSize);
    }
    @PostMapping("addDM")
    public ResponseEntity<ApiResponse> addDM(@Valid @RequestBody DanhMucResquest req){
        sv.addDM(req);
        return ResponseEntity.ok(new ApiResponse("Thêm danh mục thành công"));
    }
    @PutMapping("updateDM")
    public ResponseEntity<ApiResponse> updateDM(@RequestParam("idDanhMuc") Integer idDanhMuc,
                                                @Valid @RequestBody DanhMucResquest req){
        sv.updateDM(idDanhMuc, req);
        return ResponseEntity.ok(new ApiResponse("Update danh mục thành công"));
    }
}
