package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonChiTietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final HoaDonRepository hoaDonRepository;

    @GetMapping({"", "/hienthi"})
    public ResponseEntity<List<DTOHoaDonResponse>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonService.getAll());
    }



    @GetMapping("{id:\\d+}")
    public ResponseEntity<DTOHoaDonResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonService.findById(id));
    }

    @GetMapping("{id:\\d+}/chi-tiet")
    public ResponseEntity<List<DTOHoaDonChiTietResponse>> getChiTietByHoaDonId(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonChiTietService.getChiTietByHoaDonId(id));
    }

    @GetMapping("khach-hang/{khachHangId}")
    public ResponseEntity<List<DTOHoaDonResponse>> getByKhachHangId(@PathVariable Integer khachHangId) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonService.findByKhachHangId(khachHangId));
    }

    @PostMapping
    public ResponseEntity<DTOHoaDonResponse> add(@Valid @RequestBody DTOHoaDonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hoaDonService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<DTOHoaDonResponse> update(@PathVariable Integer id, @Valid @RequestBody DTOHoaDonRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(hoaDonService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        hoaDonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ban/{idBan}/trang-thai/{trangThai}")
    public ResponseEntity<DTOHoaDonResponse> findByBanAndStatus(
            @PathVariable Integer idBan,
            @PathVariable Integer trangThai) {

        return ResponseEntity.ok(
                hoaDonService.findByBanAndStatus(idBan, trangThai)
        );
    }
    @GetMapping("/active")
    public ResponseEntity<List<DTOHoaDonResponse>> getActiveInvoices() {
        return ResponseEntity.ok(hoaDonService.getActiveTableInvoices());
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> getActiveInvoiceCount() {
        // Đếm số lượng hóa đơn đang hoạt động trên bàn (trangThaiHoaDon = 0, trangThaiThanhToan = 0, có bàn)
        long count = hoaDonRepository.countByTrangThaiHoaDonAndTrangThaiThanhToanAndBanIsNotNull(0, 0);
        return ResponseEntity.ok(count);
    }
}

