package org.example.datlich.service;

import org.example.datlich.dto.DTOChucVuRequest;
import org.example.datlich.dto.DTOChucVuResponse;
import org.example.datlich.entity.ChucVu;
import java.util.List;

public interface ChucVuService {
    List<DTOChucVuResponse> getAll();

    DTOChucVuResponse getById(Integer id);

    DTOChucVuResponse add(DTOChucVuRequest request);

    DTOChucVuResponse update(Integer id, DTOChucVuRequest request);

    void delete(Integer id);
}
