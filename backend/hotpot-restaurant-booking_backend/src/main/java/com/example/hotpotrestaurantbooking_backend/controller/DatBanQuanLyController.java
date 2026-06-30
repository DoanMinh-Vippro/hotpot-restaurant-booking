package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dat-ban-quan-ly")
public class DatBanQuanLyController {

    private final DatBanQuanLyService datBanQuanLyService;

    @GetMapping
    public ResponseEntity<List<DTODatBanQuanLyResponse>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(datBanQuanLyService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<DTODatBanQuanLyResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(datBanQuanLyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DTODatBanQuanLyResponse> add(@Valid @RequestBody DTODatBanQuanLyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datBanQuanLyService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<DTODatBanQuanLyResponse> update(@PathVariable Integer id, @Valid @RequestBody DTODatBanQuanLyRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(datBanQuanLyService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        datBanQuanLyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}