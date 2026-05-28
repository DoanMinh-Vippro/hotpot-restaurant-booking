// ===========================================
// KhachHangController
// ===========================================
package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/khach-hang")
@CrossOrigin("*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping("/hien-thi")
    public List<KhachHang> getAll() {
        return khachHangService.getAll();
    }

    @GetMapping("/detail/{id}")
    public KhachHang getById(@PathVariable Integer id) {
        return khachHangService.getById(id);
    }

    @PostMapping("/add")
    public KhachHang add(@RequestBody KhachHang khachHang) {
        return khachHangService.add(khachHang);
    }

    @PutMapping("/update/{id}")
    public KhachHang update(@PathVariable Integer id,
                            @RequestBody KhachHang khachHang) {
        return khachHangService.update(id, khachHang);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        khachHangService.delete(id);
    }
}