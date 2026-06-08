package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOChucVuResponse;
import com.example.hotpotrestaurantbooking_backend.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chuc-vu")
@CrossOrigin(origins = "*")
public class ChucVuController {
    @Autowired
    private ChucVuService service;

    @GetMapping("/hien-thi")
    public List<DTOChucVuResponse> getAll(){
        return service.getAll();
    }

    @GetMapping("/detail/{id}")
    public DTOChucVuResponse findById(@PathVariable Integer id){
        return service.findById(id);
    }

    @PostMapping("/add")
    public DTOChucVuResponse add(@RequestBody DTOChucVuRequest req){
        return service.add(req);
    }

    @PutMapping("/update/{id}")
    public DTOChucVuResponse update(@PathVariable Integer id,
                                    @RequestBody DTOChucVuRequest req){
        return service.update(id, req);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        service.delete(id);
    }
}
