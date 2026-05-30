package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.ChiTietComBoResponse;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietComboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChiTietComBoService {
    @Autowired
    private ChiTietComboRepository repo;

    public List<ChiTietComBoResponse>hienThi(){
        return repo.hienThi();
    }
}
