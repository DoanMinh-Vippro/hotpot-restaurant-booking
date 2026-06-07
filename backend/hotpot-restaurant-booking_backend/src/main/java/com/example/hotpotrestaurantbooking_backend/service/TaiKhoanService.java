package org.example.datlich.service;

import org.example.datlich.dto.DTOTaiKhoanRequest;
import org.example.datlich.dto.DTOTaiKhoanResponse;
import org.example.datlich.entity.TaiKhoan;

import java.util.List;

public interface TaiKhoanService {
    List<DTOTaiKhoanResponse> getAll();

    DTOTaiKhoanResponse getById(Integer id);

    DTOTaiKhoanResponse add(DTOTaiKhoanRequest request);

    DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest
            request);

    void delete(Integer id);
}
