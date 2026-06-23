
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TaiKhoanServiceImpl implements TaiKhoanService {
    private final ModelMapper mapper;
    private final TaiKhoanRepository taiKhoanRepository;
    private DTOTaiKhoanResponse toDTO(TaiKhoan t) {
        DTOTaiKhoanResponse dto = new DTOTaiKhoanResponse();

        dto.setId(t.getIdTaiKhoan());
        dto.setMaTaiKhoan(t.getMaTaiKhoan());
        dto.setTenDangNhap(t.getTenDangNhap());
        dto.setMatKhau(t.getMatKhau());
        dto.setTrangThai(t.getTrangThai());

        if (t.getChucVu() != null) {
            dto.setIdChucVu(t.getChucVu().getIdChucVu());
            dto.setTenChucVu(t.getChucVu().getTenChucVu());
        }

        return dto;
    }
    @Override
    public List<DTOTaiKhoanResponse> getAll() {  // ← Đổi thành getAll
        return taiKhoanRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public DTOTaiKhoanResponse findById(Integer id) {
        return taiKhoanRepository.findById(id)
                .map(t -> {
                    DTOTaiKhoanResponse dto = new DTOTaiKhoanResponse();

                    dto.setId(t.getIdTaiKhoan());
                    dto.setMaTaiKhoan(t.getMaTaiKhoan());
                    dto.setTenDangNhap(t.getTenDangNhap());
                    dto.setMatKhau(t.getMatKhau());
                    dto.setTrangThai(t.getTrangThai());

                    if (t.getChucVu() != null) {
                        dto.setIdChucVu(t.getChucVu().getIdChucVu());
                        dto.setTenChucVu(t.getChucVu().getTenChucVu());
                    }

                    return dto;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay tai khoan nay"));
    }

    @Override
    public DTOTaiKhoanResponse add(DTOTaiKhoanRequest tk) {

        if (taiKhoanRepository.existsByTenDangNhap(tk.getTenDangNhap())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        TaiKhoan t = mapper.map(tk, TaiKhoan.class);

        ChucVu cv = new ChucVu();
        cv.setIdChucVu(tk.getIdChucVu());
        t.setChucVu(cv);

        t = taiKhoanRepository.save(t);

        t.setMaTaiKhoan(String.format("TK%03d", t.getIdTaiKhoan()));

        return toDTO(taiKhoanRepository.save(t));
    }

    @Override
   public DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest tk) {
        return taiKhoanRepository.findById(id)
                .map(t -> {

                    if (tk.getTenDangNhap() != null)
                        t.setTenDangNhap(tk.getTenDangNhap());

                    if (tk.getMatKhau() != null)
                        t.setMatKhau(   tk.getMatKhau());

                    if (tk.getTrangThai() != null)
                        t.setTrangThai(tk.getTrangThai());

                    if (tk.getIdChucVu() != null) {
                        ChucVu cv = new ChucVu();
                        cv.setIdChucVu(tk.getIdChucVu());
                        t.setChucVu(cv);
                    }

                    return toDTO(taiKhoanRepository.save(t));
                })
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("khong tim thay tai khoan nay de sua"));
    }

    @Override
    public void delete(Integer id) {
        taiKhoanRepository.deleteById(id);
    }
}
