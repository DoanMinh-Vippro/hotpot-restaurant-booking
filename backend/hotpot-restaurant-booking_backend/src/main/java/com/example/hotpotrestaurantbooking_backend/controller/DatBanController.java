package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/dat-bans")
@RestController
public class DatBanController {
    private final DatBanService datBanService;

    @GetMapping
    public ResponseEntity<List<DTODatBanResponse>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<DTODatBanResponse> findById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DTODatBanResponse> add(@Valid @RequestBody DTODatBanRequest dtoDatBanRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(datBanService.add(dtoDatBanRequest));
    }

    @PutMapping("{id}")
    public ResponseEntity<DTODatBanResponse> update(@PathVariable Integer id, @Valid @RequestBody DTODatBanRequest dtoDatBanRequest){
        return ResponseEntity.status(HttpStatus.OK).body(datBanService.update(id,dtoDatBanRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        datBanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
