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

                .chucVu(chucVuRepo.findById(req.getIdChucVu())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ")))

                .taiKhoan(taiKhoanRepo.findById(req.getIdTaiKhoan())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản")))
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
                .diaChi(nv.getDiaChi())
                .trangThai(nv.getTrangThai())

                .idChucVu(nv.getChucVu() != null ? nv.getChucVu().getIdChucVu() : null)
                .tenChucVu(nv.getChucVu() != null ? nv.getChucVu().getTenChucVu() : null)

                .idTaiKhoan(nv.getTaiKhoan() != null ? nv.getTaiKhoan().getIdTaiKhoan() : null)
                .tenDangNhap(nv.getTaiKhoan() != null ? nv.getTaiKhoan().getTenDangNhap() : null)
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
    public DTONhanVienResponse findByTaiKhoanId(Integer idTaiKhoan) {
        NhanVien nv = repository.findByTaiKhoan_IdTaiKhoan(idTaiKhoan);
        if (nv == null) {
            throw new RuntimeException("Không tìm thấy nhân viên theo tài khoản này");
        }
        return toResponse(nv);
    }

    @Override
    public DTONhanVienResponse update(Integer id, DTONhanVienRequest request) {
        NhanVien old = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (request.getMaNhanVien() != null)
            old.setMaNhanVien(request.getMaNhanVien());

        if (request.getTenNhanVien() != null)
            old.setTenNhanVien(request.getTenNhanVien());

        if (request.getGioiTinh() != null)
            old.setGioiTinh(request.getGioiTinh());

        if (request.getSoDienThoai() != null)
            old.setSoDienThoai(request.getSoDienThoai());

        if (request.getEmail() != null)
            old.setEmail(request.getEmail());

        if (request.getDiaChi() != null)
            old.setDiaChi(request.getDiaChi());

        if (request.getTrangThai() != null)
            old.setTrangThai(request.getTrangThai());

        if (request.getIdChucVu() != null)
            old.setChucVu(
                    chucVuRepo.findById(request.getIdChucVu())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"))
            );

        if (request.getIdTaiKhoan() != null)
            old.setTaiKhoan(
                    taiKhoanRepo.findById(request.getIdTaiKhoan())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"))
            );

        return toResponse(repository.save(old));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
