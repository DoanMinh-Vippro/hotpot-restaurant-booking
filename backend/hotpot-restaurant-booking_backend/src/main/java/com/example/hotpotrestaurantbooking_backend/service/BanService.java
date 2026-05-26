// ===========================================
// BanService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.Ban;

import java.util.List;

public interface BanService {

    List<Ban> getAll();

    Ban getById(Integer id);

    Ban add(Ban ban);

    Ban update(Integer id, Ban ban);

    void delete(Integer id);
}