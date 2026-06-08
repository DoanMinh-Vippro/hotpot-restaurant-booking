<<<<<<< HEAD
// ===========================================
// TaiKhoanService
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
=======
package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;

>>>>>>> 6741c3e41b67912050083fa6b80f7e4b3d98e35f

import java.util.List;

public interface TaiKhoanService {
    List<DTOTaiKhoanResponse> getAll();
<<<<<<< HEAD
    DTOTaiKhoanResponse findById(Integer id);
    DTOTaiKhoanResponse add(DTOTaiKhoanRequest tk);
    DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest tk);
=======

    DTOTaiKhoanResponse findById(Integer id);

    DTOTaiKhoanResponse add(DTOTaiKhoanRequest request);

    DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest
            request);

>>>>>>> 6741c3e41b67912050083fa6b80f7e4b3d98e35f
    void delete(Integer id);
}