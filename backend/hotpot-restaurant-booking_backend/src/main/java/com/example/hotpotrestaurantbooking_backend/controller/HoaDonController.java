package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.HoaDonChiTietDTO;
import com.example.hotpotrestaurantbooking_backend.dto.HoaDonDTO;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don") // Thêm /api để tránh xung đột với file tĩnh
@CrossOrigin("*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    // Gọi: GET http://localhost:8080/api/hoa-don hoặc /api/hoa-don/hienthi
    @GetMapping({"", "/hienthi"})
    public List<HoaDonDTO> getAll() {
        return hoaDonService.getAll();
    }

    // Gọi: GET http://localhost:8080/api/hoa-don/1
    @GetMapping("/{id}")
    public HoaDonDTO getById(@PathVariable Integer id) {
        return hoaDonService.getById(id);
    }

    // Gọi: GET http://localhost:8080/api/hoa-don/1/chi-tiet
    @GetMapping("/{id}/chi-tiet")
    public List<HoaDonChiTietDTO> getChiTietByHoaDonId(@PathVariable Integer id) {
        return hoaDonService.getChiTietByHoaDonId(id);
    }

    // Gọi: POST http://localhost:8080/api/hoa-don
    @PostMapping
    public HoaDon add(@RequestBody HoaDon hoaDon) {
        return hoaDonService.add(hoaDon);
    }

    // Gọi: PUT http://localhost:8080/api/hoa-don/1
    @PutMapping("/{id}")
    public HoaDon update(@PathVariable Integer id, @RequestBody HoaDon hoaDon) {
        return hoaDonService.update(id, hoaDon);
    }

    // Gọi: DELETE http://localhost:8080/api/hoa-don/1
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        hoaDonService.delete(id);
    }
}
