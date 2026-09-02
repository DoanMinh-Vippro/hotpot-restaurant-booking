package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import com.example.hotpotrestaurantbooking_backend.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/dat-bans")
@RestController
public class DatBanController {
    private final DatBanService datBanService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<DTODatBanResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.getAll());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/check-ban")
    public ResponseEntity<DTOCheckBanResponse> checkBan(@Valid @RequestBody DTOCheckBanRequest request){
        return ResponseEntity.ok(datBanService.checkBan(request));
    }

    @GetMapping("/danh-sach-ban-co-the-chon")
    public ResponseEntity<List<DTOBanResponse>> getDanhSachBanCoTheChon(@RequestParam LocalDateTime thoiGianDenDuKien) {
        return ResponseEntity.ok(datBanService.getDanhSachBanCoTheChon(thoiGianDenDuKien));
    }

    @GetMapping("{id}")
    public ResponseEntity<DTODatBanResponse> findById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DTODatBanResponse> add(@Valid @RequestBody DTODatBanRequest dtoDatBanRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(datBanService.add(dtoDatBanRequest));
    }

    @PutMapping("{id}")
    public ResponseEntity<DTODatBanResponse> update(@PathVariable Integer id, @Valid @RequestBody DTODatBanRequest dtoDatBanRequest){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.update(id,dtoDatBanRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        datBanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tinh-trang-ban")
    public ResponseEntity<DTOTinhTrangBanResponse> tinhTrangBan(@RequestParam LocalDateTime thoiGianDenDuKien) {
        return ResponseEntity.ok(datBanService.tinhTrangBan(thoiGianDenDuKien));
    }
}
