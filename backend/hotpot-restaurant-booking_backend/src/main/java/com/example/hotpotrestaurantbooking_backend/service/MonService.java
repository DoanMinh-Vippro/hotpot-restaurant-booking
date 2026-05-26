// ===========================================
// MonService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.Mon;

import java.util.List;

public interface MonService {

    List<Mon> getAll();

    Mon getById(Integer id);

    Mon add(Mon mon);

    Mon update(Integer id, Mon mon);

    void delete(Integer id);
}