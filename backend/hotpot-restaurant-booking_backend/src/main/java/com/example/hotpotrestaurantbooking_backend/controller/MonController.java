// ===========================================
// MonController
// ===========================================
package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.service.MonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mon")
@CrossOrigin("*")
public class MonController {

    @Autowired
    private MonService monService;

    @GetMapping("/hien-thi")
    public List<Mon> getAll() {
        return monService.getAll();
    }

    @GetMapping("/detail/{id}")
    public Mon getById(@PathVariable Integer id) {
        return monService.getById(id);
    }

    @PostMapping("/add")
    public Mon add(@RequestBody Mon mon) {
        return monService.add(mon);
    }

    @PutMapping("/update/{id}")
    public Mon update(@PathVariable Integer id,
                      @RequestBody Mon mon) {
        return monService.update(id, mon);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        monService.delete(id);
    }
}