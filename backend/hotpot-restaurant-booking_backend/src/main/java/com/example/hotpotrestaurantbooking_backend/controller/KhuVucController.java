package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.KhuVucRequest;
import com.example.hotpotrestaurantbooking_backend.dto.KhuVucResponse;
import com.example.hotpotrestaurantbooking_backend.service.KhuVucService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khu_vuc")
@CrossOrigin(origins = "http://localhost:5173")

@RequiredArgsConstructor
public class KhuVucController {
    private final KhuVucService khuVucService;
    @GetMapping
    public ResponseEntity<List<KhuVucResponse>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(khuVucService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<KhuVucResponse> findById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(khuVucService.findById(id));
    }
    @PostMapping
    public ResponseEntity<KhuVucResponse> add(@Valid @RequestBody KhuVucRequest khuVucRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(khuVucService.add(khuVucRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<KhuVucResponse> update(@Valid @RequestBody KhuVucRequest khuVucRequest,@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(khuVucService.update(khuVucRequest, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        khuVucService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/change-status/{id}")
    public ResponseEntity<KhuVucResponse> changeStatus(@PathVariable Integer id){
        return ResponseEntity.ok(khuVucService.changeStatus(id));
    }
//    @GetMapping("/search")
//    public List<KhuVuc> search(@RequestParam String keyword) {
//        return khuVucService.search(keyword);
//    }
}
