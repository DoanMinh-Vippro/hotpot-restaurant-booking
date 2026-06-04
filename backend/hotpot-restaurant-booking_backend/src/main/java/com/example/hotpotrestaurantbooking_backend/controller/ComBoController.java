package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.ComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ComboResponse;
import com.example.hotpotrestaurantbooking_backend.service.ComBoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
public class ComBoController {
    @Autowired
    private ComBoService sv;

    @GetMapping("hienThiComBo")
    public List<ComboResponse> hienThiComBo(){
        return sv.hienThiComBo();
    }
    @GetMapping("detailComBo")
    public ComboResponse detailComBo(@RequestParam("tenCombo") String tenCombo){
        return sv.detailComBo(tenCombo);
    }
    @GetMapping("phanTrangComBo")
    public Page<ComboResponse> phanTrangComBo(@RequestParam(defaultValue = "0") Integer pageNo,
                                              @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.phanTrangComBo(pageNo, pageSize);
    }
    @GetMapping("timKiemComBo")
    public Page<ComboResponse>timKiemComBo(@RequestParam(required = false) String tenCombo,
                                           @RequestParam(required = false) BigDecimal giaMin,
                                           @RequestParam(required = false) BigDecimal giaMax,
                                           @RequestParam(defaultValue = "0") Integer pageNo,
                                           @RequestParam(defaultValue = "5") Integer pageSize){
        return sv.timKiemComBo(tenCombo, giaMin, giaMax, pageNo, pageSize);
    }
    @PostMapping("addComBo")
    public ResponseEntity<ApiResponse>addComBo(@Valid @RequestBody ComboRequest req){
            sv.addComBo(req);
            return ResponseEntity.ok(new ApiResponse("Thêm ComBo thành công"));
    }
    @PutMapping("updateComBo")
    public ResponseEntity<ApiResponse>updateComBo(@RequestParam("idCombo") Integer idCombo,
                                                  @Valid @RequestBody ComboRequest req){
        sv.updateComBo(idCombo, req);
        return ResponseEntity.ok(new ApiResponse("Update ComBo thành công"));
    }
    @DeleteMapping("deleteComBo")
    public ResponseEntity<ApiResponse>deleteComBo(@RequestParam("idCombo") Integer idCombo){
        sv.deleteComBo(idCombo);
        return ResponseEntity.ok(new ApiResponse("Đã ngưng bán ComBo này"));
    }
}
