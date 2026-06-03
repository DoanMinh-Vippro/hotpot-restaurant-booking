package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.Validation.ApiResponse;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaDTO;
import com.example.hotpotrestaurantbooking_backend.dto.GiamGiaRequest;
import com.example.hotpotrestaurantbooking_backend.service.GiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/giam-gia")
public class GiamGiaController {

    @Autowired
    private GiamGiaService service;

    @GetMapping
    public List<GiamGiaDTO> getAll(@RequestParam(required = false) String keyword) {
        return service.getAll(keyword);
    }

    @GetMapping("/{id}")
    public GiamGiaDTO getById(@PathVariable("id") Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody GiamGiaRequest request) {
        service.createGiamGia(request);
        return ResponseEntity.ok(new ApiResponse("Thêm mã giảm giá thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable("id") Integer id,
                                              @Valid @RequestBody GiamGiaRequest request) {
        service.updateGiamGia(id, request);
        return ResponseEntity.ok(new ApiResponse("Cập nhật mã giảm giá thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable("id") Integer id) {
        service.deleteGiamGia(id);
        return ResponseEntity.ok(new ApiResponse("Xóa mã giảm giá thành công"));
    }
}
