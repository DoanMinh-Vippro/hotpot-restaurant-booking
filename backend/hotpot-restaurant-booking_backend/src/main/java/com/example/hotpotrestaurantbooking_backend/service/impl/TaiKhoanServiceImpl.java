
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TaiKhoanServiceImpl implements TaiKhoanService {
    private final ModelMapper mapper;
    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public List<DTOTaiKhoanResponse> getAll() {
        return taiKhoanRepository
                .findAll()
                .stream()
                .map(t -> mapper.map(t, DTOTaiKhoanResponse.class))
                .toList();
    }

    @Override
    public DTOTaiKhoanResponse findById(Integer id) {
        return taiKhoanRepository
                .findById(id)
                .map(t -> mapper.map(t,DTOTaiKhoanResponse.class))
                .orElseThrow(()->new CustomResourceNotFoundException("khong tim thay tai khoan nay"));
    }

    @Override
    public DTOTaiKhoanResponse add(DTOTaiKhoanRequest tk) {
        if (taiKhoanRepository.existsByTenDangNhap(tk.getTenDangNhap())){
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        TaiKhoan t = mapper.map(tk,TaiKhoan.class);
        taiKhoanRepository.save(t);
        t.setMaTaiKhoan(String.format("TK%03d", t.getIdTaiKhoan()));
        taiKhoanRepository.save(t);
        return mapper.map(t,DTOTaiKhoanResponse.class);
    }

    @Override
    public DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest tk) {
        return taiKhoanRepository
                .findById(id)
                .map(t -> {
                    if (tk.getTenDangNhap()!=null) t.setTenDangNhap(tk.getTenDangNhap());
                    if (tk.getMatKhau()!=null) t.setMatKhau(tk.getMatKhau());
                    taiKhoanRepository.save(t);
                    return mapper.map(t,DTOTaiKhoanResponse.class);
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay tai khoan nay de sua"));
    }

    @Override
    public void delete(Integer id) {
        taiKhoanRepository.deleteById(id);
    }
}
