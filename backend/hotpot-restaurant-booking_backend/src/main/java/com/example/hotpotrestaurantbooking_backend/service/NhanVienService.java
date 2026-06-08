package org.example.datlich.service;

import org.example.datlich.dto.DTONhanVienRequest;
import org.example.datlich.dto.DTONhanVienResponse;


import java.util.List;

public interface NhanVienService {
    List<DTONhanVienResponse> getAll();

    DTONhanVienResponse getById(Integer id);

    DTONhanVienResponse add(DTONhanVienRequest request);

    DTONhanVienResponse update(Integer id, DTONhanVienRequest request);

    void delete(Integer id);
}
