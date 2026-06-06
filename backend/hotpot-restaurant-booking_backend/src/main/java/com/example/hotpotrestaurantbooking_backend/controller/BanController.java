package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTOBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;
import com.example.hotpotrestaurantbooking_backend.service.BanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bans")
@RequiredArgsConstructor
public class BanController {
    private final BanService banService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DTOBanResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(banService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<DTOBanResponse> findById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(banService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DTOBanResponse> add(@Valid @RequestBody DTOBanRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(banService.add(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<DTOBanResponse> update(@PathVariable Integer id, @Valid @RequestBody DTOBanRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(banService.update(id,request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        banService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<DTOBanResponse>> search(@RequestParam(required = false) String key){
        return ResponseEntity.status(HttpStatus.OK).body(banService.search(key,key));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sort")
    public ResponseEntity<List<DTOBanResponse>> sort(){
        return ResponseEntity.status(HttpStatus.OK).body(banService.sort());
    }
}
