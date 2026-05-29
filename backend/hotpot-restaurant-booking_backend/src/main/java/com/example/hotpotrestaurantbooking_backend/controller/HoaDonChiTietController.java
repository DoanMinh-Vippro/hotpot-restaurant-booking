package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonChiTietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don-chi-tiet")
@CrossOrigin("*")
@RequiredArgsConstructor
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    @GetMapping
    public ResponseEntity<List<DTOHoaDonChiTietResponse>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonChiTietService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<DTOHoaDonChiTietResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonChiTietService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DTOHoaDonChiTietResponse> add(@Valid @RequestBody DTOHoaDonChiTietRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hoaDonChiTietService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<DTOHoaDonChiTietResponse> update(@PathVariable Integer id, @Valid @RequestBody DTOHoaDonChiTietRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonChiTietService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        hoaDonChiTietService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
