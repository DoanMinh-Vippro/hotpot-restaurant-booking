package com.example.hotpotrestaurantbooking_backend.controller;




import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienResponse;
import com.example.hotpotrestaurantbooking_backend.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/nhan-vien")
@CrossOrigin(origins = "*")
public class NhanVienController {
    @Autowired
    private NhanVienService service;

    @GetMapping("/hien-thi")
    public List<DTONhanVienResponse> getAll(){
        return service.getAll();
    }

    @GetMapping("/detail/{id}")
    public DTONhanVienResponse findById(@PathVariable Integer id){
        return service.findById(id);
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
