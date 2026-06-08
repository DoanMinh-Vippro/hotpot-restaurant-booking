package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienResponse;
import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.repository.NhanVienRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRespository;
import com.example.hotpotrestaurantbooking_backend.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NhanVienServiceImpl implements NhanVienService {
    @Autowired
    private NhanVienRepository repository;

    @Autowired
    private ChucVuRepository chucVuRepo;

    @Autowired
    private TaiKhoanRespository taiKhoanRepo;

    private NhanVien toEntity(DTONhanVienRequest req){
        return NhanVien.builder()
                .maNhanVien(req.getMaNhanVien())
                .tenNhanVien(req.getTenNhanVien())
                .gioiTinh(req.getGioiTinh())
                .soDienThoai(req.getSoDienThoai())
                .email(req.getEmail())
                .diaChi(req.getDiaChi())
                .trangThai(req.getTrangThai())
                .chucVu(chucVuRepo.findById(req.getIdChucVu()).orElse(null))
                .taiKhoan(taiKhoanRepo.findById(req.getIdTaiKhoan()).orElse(null))
                .build();
    }


    private DTONhanVienResponse toResponse(NhanVien nv){
        return DTONhanVienResponse.builder()
                .id(nv.getId())
                .maNhanVien(nv.getMaNhanVien())
                .tenNhanVien(nv.getTenNhanVien())
                .gioiTinh(nv.getGioiTinh())
                .soDienThoai(nv.getSoDienThoai())
                .email(nv.getEmail())

                .idChucVu(nv.getChucVu() != null ? nv.getChucVu().getIdChucVu() : null)
                .idTaiKhoan(nv.getTaiKhoan() != null ? nv.getTaiKhoan().getIdTaiKhoan() : null)
                .tenChucVu(nv.getChucVu() != null ? nv.getChucVu().getTenChucVu() : null)
                .tenDangNhap(nv.getTaiKhoan() != null ? nv.getTaiKhoan().getTenDangNhap() : null)
                .diaChi(nv.getDiaChi())
                .trangThai(nv.getTrangThai())
                .build();
    }

    @Override
    public List<DTONhanVienResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DTONhanVienResponse findById(Integer id) {
        NhanVien nv = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        return toResponse(nv);
    }

    @Override
    public DTONhanVienResponse add(DTONhanVienRequest request) {
        NhanVien nv = toEntity(request);
        return toResponse(repository.save(nv));
    }

    @Override
    public DTONhanVienResponse update(Integer id, DTONhanVienRequest request) {
        NhanVien old = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        old.setMaNhanVien(request.getMaNhanVien());
        old.setTenNhanVien(request.getTenNhanVien());
        old.setGioiTinh(request.getGioiTinh());
        old.setSoDienThoai(request.getSoDienThoai());
        old.setEmail(request.getEmail());
        old.setDiaChi(request.getDiaChi());
        old.setTrangThai(request.getTrangThai());
        old.setChucVu(chucVuRepo.findById(request.getIdChucVu()).orElse(null));
        old.setTaiKhoan(taiKhoanRepo.findById(request.getIdTaiKhoan()).orElse(null));

        return toResponse(repository.save(old));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
