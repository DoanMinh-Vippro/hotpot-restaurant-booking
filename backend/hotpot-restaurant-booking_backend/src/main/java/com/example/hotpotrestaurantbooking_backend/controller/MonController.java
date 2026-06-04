// ===========================================
// MonController
// ===========================================
package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.MonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.MonResponse;
import com.example.hotpotrestaurantbooking_backend.service.MonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
public class MonController {

    @Autowired
    private MonService monService;

    @GetMapping("hienThiMon")
    public List<MonResponse> hienThiMon() {
        return monService.hienThiMon();
    }

    @GetMapping("detailMon")
    public MonResponse detailMon(@RequestParam("tenMon") String tenMon){
        return monService.detailMon(tenMon);
    }
    @GetMapping("phanTrangMon")
    public Page<MonResponse> phanTrangMon(@RequestParam(defaultValue = "0") Integer pageNo,
                                          @RequestParam(defaultValue = "5") Integer pageSize){
        return monService.phanTrangMon(pageNo,pageSize);
    }

    @GetMapping("searchMon")
    public Page<MonResponse> timKiemMon(
            @RequestParam(required = false) String tenMon,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) String loaiDanhMuc,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return monService.timKiemMon(
                tenMon,
                giaMin,
                giaMax,
                loaiDanhMuc,
                pageNo,
                pageSize
        );
    }
    @PostMapping("addMon")
    public ResponseEntity<ApiResponse> addMon(@Valid @RequestBody MonRequest req){
        monService.addMon(req);
        return ResponseEntity.ok(new ApiResponse("Thêm món thành công"));
    }
    @PutMapping("updateMon")
    public ResponseEntity<ApiResponse> updateMon(@RequestParam("idMon") Integer idMon, @Valid @RequestBody MonRequest req){
        monService.updateMon(idMon, req);
        return ResponseEntity.ok(new ApiResponse("Update món thành công"));
    }
    @DeleteMapping("deleteMon")
    public ResponseEntity<ApiResponse> deleteMon(@RequestParam("idMon") Integer idMon){
        monService.deleteMon(idMon);
        return ResponseEntity.ok(new ApiResponse("Đã ngưng bán món này"));
    }
}