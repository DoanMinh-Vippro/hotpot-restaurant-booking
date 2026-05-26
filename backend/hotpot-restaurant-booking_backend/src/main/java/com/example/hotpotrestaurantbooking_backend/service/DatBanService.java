// ===========================================
// DatBanService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.DatBan;

import java.util.List;

public interface DatBanService {

    List<DatBan> getAll();

    DatBan getById(Integer id);

    DatBan add(DatBan datBan);

    DatBan update(Integer id, DatBan datBan);

    void delete(Integer id);
}