package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.KhachHangRequest;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangResponse;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khach_hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

public class KhachHangController {
    private final KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangResponse>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(khachHangService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangResponse> findById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(khachHangService.findById(id));
    }
    @PostMapping
    public ResponseEntity<KhachHangResponse> add(@Valid @RequestBody KhachHangRequest khachHangRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(khachHangService.add(khachHangRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<KhachHangResponse> update(@Valid @RequestBody KhachHangRequest khachHangRequest,@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(khachHangService.update(khachHangRequest, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        khachHangService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public List<KhachHang> search(@RequestParam String keyword) {
        return khachHangService.search(keyword);
    }
}
