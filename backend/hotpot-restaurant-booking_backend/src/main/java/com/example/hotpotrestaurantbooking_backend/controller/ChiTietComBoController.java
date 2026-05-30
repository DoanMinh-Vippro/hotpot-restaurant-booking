package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietComBoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
public class ChiTietComBoController {
    @Autowired
    private ChiTietComBoService sv;

    @GetMapping("hienThiCTCB")
    public List<ChiTietComBoResponse>hienThi(){
        return sv.hienThi();
    }
}
