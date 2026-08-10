package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dat-ban-quan-ly")
public class DatBanQuanLyController {

    private final DatBanQuanLyService datBanQuanLyService;

    @GetMapping
    public ResponseEntity<List<DTODatBanQuanLyResponse>> getAll() {
        return ResponseEntity.ok(datBanQuanLyService.getAll());
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
    public ResponseEntity<DTODatBanQuanLyResponse> update(@PathVariable Integer id,@Valid @RequestBody DTODatBanQuanLyRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(datBanQuanLyService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        datBanQuanLyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ban-trong")
    public List<DTOBanResponse> getDanhSachBanTrong(@RequestParam LocalDateTime thoiGianDenDuKien,
                                                    @RequestParam Integer soNguoi,
            @RequestParam(required = false) Integer idDatBan) {
        return datBanQuanLyService.getDanhSachBanTrong(thoiGianDenDuKien, soNguoi, idDatBan);
    }

    @PutMapping("/{id}/xac-nhan")
    public ResponseEntity<DTODatBanQuanLyResponse> xacNhan(@PathVariable Integer id) {
        return ResponseEntity.ok(datBanQuanLyService.xacNhan(id));
    }

    @PutMapping("/{id}/check-in")
    public ResponseEntity<DTODatBanQuanLyResponse> checkIn(@PathVariable Integer id) {
        return ResponseEntity.ok(datBanQuanLyService.checkIn(id));
    }

    @PutMapping("/{id}/doi-gio")
    public ResponseEntity<DTODatBanQuanLyResponse> doiGio(@PathVariable Integer id, @RequestBody @Valid DTODoiGioRequest request) {
        return ResponseEntity.ok(datBanQuanLyService.doiGio(id, request));
    }

    @PutMapping("/{id}/doi-ban")
    public ResponseEntity<DTODatBanQuanLyResponse> doiBan(@PathVariable Integer id,
                                                          @RequestBody @Valid DTODoiBanRequest request) {
        return ResponseEntity.ok(datBanQuanLyService.doiBan(id, request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DTODatBanQuanLyResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TrangThaiDatBan trangThai,
            @RequestParam(required = false) LocalDate tuNgay,
            @RequestParam(required = false) LocalDate denNgay) {
        return ResponseEntity.ok(datBanQuanLyService.search(keyword, trangThai,
                tuNgay == null ? null : tuNgay.atStartOfDay(), denNgay == null ? null : denNgay.atTime(LocalTime.MAX))
        );
    }

    @PutMapping("/{id}/hoan-thanh")
    public ResponseEntity<DTODatBanQuanLyResponse> hoanThanh(@PathVariable Integer id) {
        return ResponseEntity.ok(datBanQuanLyService.hoanThanh(id));
    }

    @GetMapping("/tinh-trang-ban")
    public ResponseEntity<List<DTOBanResponse>> tinhTrangBan(@RequestParam LocalDateTime thoiGianDenDuKien) {
        return ResponseEntity.ok(datBanQuanLyService.tinhTrangBan(thoiGianDenDuKien));
    }
}