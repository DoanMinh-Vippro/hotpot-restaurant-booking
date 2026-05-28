// ===========================================
// DatBanController
// ===========================================
package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dat-ban")
@CrossOrigin("*")
public class DatBanController {

    @Autowired
    private DatBanService datBanService;

    @GetMapping("/hien-thi")
    public List<DatBan> getAll() {
        return datBanService.getAll();
    }

    @GetMapping("/detail/{id}")
    public DatBan getById(@PathVariable Integer id) {
        return datBanService.getById(id);
    }

    @PostMapping("/add")
    public DatBan add(@RequestBody DatBan datBan) {
        return datBanService.add(datBan);
    }

    @PutMapping("/update/{id}")
    public DatBan update(@PathVariable Integer id,
                         @RequestBody DatBan datBan) {
        return datBanService.update(id, datBan);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        datBanService.delete(id);
    }
}