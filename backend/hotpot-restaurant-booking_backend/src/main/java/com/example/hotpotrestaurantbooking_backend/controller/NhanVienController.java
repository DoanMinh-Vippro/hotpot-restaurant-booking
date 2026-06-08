package com.example.hotpotrestaurantbooking_backend.controller;

import org.example.datlich.dto.DTONhanVienRequest;
import org.example.datlich.dto.DTONhanVienResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nhan-vien")
@CrossOrigin(origins = "*")
public class NhanVienController {
    @Autowired
    private NhanVienService service;

    @GetMapping("/hien-thi")
    public List<DTONhanVienResponse> getAll(){
        return service.getAll();
    }

    @GetMapping("/detail/{id}")
    public DTONhanVienResponse getById(@PathVariable Integer id){
        return service.getById(id);
    }

    @PostMapping("/add")
    public DTONhanVienResponse add(@RequestBody DTONhanVienRequest request){
        return service.add(request);
    }

    @PutMapping("/update/{id}")
    public DTONhanVienResponse update(@PathVariable Integer id,
                                      @RequestBody DTONhanVienRequest request){
        return service.update(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        service.delete(id);
    }
}
