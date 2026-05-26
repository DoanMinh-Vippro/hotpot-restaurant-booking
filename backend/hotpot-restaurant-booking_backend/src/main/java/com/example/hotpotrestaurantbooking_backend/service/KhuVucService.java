// ===========================================
// KhuVucService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.entity.KhuVuc;

import java.util.List;

public interface KhuVucService {

    List<KhuVuc> getAll();

    KhuVuc getById(Integer id);

    KhuVuc add(KhuVuc khuVuc);

    KhuVuc update(Integer id, KhuVuc khuVuc);

    void delete(Integer id);
}