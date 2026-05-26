// ===========================================
// BanController
// ===========================================
package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.service.BanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ban")
@CrossOrigin("*")
public class BanController {

    @Autowired
    private BanService banService;

    @GetMapping("/hien-thi")
    public List<Ban> getAll() {
        return banService.getAll();
    }

    @GetMapping("/detail/{id}")
    public Ban getById(@PathVariable Integer id) {
        return banService.getById(id);
    }

    @PostMapping("/add")
    public Ban add(@RequestBody Ban ban) {
        return banService.add(ban);
    }

    @PutMapping("/update/{id}")
    public Ban update(@PathVariable Integer id,
                      @RequestBody Ban ban) {
        return banService.update(id, ban);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        banService.delete(id);
    }
}