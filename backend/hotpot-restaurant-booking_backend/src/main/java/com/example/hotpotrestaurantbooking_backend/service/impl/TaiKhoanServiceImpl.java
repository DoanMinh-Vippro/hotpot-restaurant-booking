
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.repository.NhanVienRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class TaiKhoanServiceImpl implements TaiKhoanService {
  private final ModelMapper mapper;
    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
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
   @Transactional
   public DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest tk) {
        return taiKhoanRepository.findById(id)
                .map(t -> {
                    Integer oldRoleId = t.getChucVu() != null ? t.getChucVu().getIdChucVu() : null;

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

                    TaiKhoan saved = taiKhoanRepository.save(t);
                    syncLinkedProfiles(saved, oldRoleId, tk.getIdChucVu());
                    return toDTO(saved);
                })
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("khong tim thay tai khoan nay de sua"));
    }

    private void syncLinkedProfiles(TaiKhoan taiKhoan, Integer oldRoleId, Integer newRoleId) {
        if (newRoleId != null && Objects.equals(oldRoleId, newRoleId)) {
            return;
        }

        KhachHang khachHang = khachHangRepository.findByTaiKhoan_IdTaiKhoan(taiKhoan.getIdTaiKhoan());
        if (khachHang != null) {
            if (newRoleId != null && newRoleId != 3) {
                khachHang.setTrangThai(false);
            } else {
                khachHang.setTrangThai(true);
            }
            khachHangRepository.save(khachHang);
        }

        NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_IdTaiKhoan(taiKhoan.getIdTaiKhoan());
        if (newRoleId != null && newRoleId == 2) {
            if (nhanVien == null) {
                nhanVien = new NhanVien();
                nhanVien.setMaNhanVien(String.format("NV%03d", taiKhoan.getIdTaiKhoan()));
            }

            nhanVien.setTaiKhoan(taiKhoan);
            nhanVien.setChucVu(taiKhoan.getChucVu());
            nhanVien.setTrangThai(true);

            if (khachHang != null) {
                nhanVien.setTenNhanVien(khachHang.getTenKhachHang());
                nhanVien.setGioiTinh(khachHang.getGioiTinh());
                nhanVien.setSoDienThoai(khachHang.getSoDienThoai());
                nhanVien.setEmail(khachHang.getEmail());
                nhanVien.setDiaChi(khachHang.getDiaChi());
            } else {
                if (nhanVien.getTenNhanVien() == null) {
                    nhanVien.setTenNhanVien(taiKhoan.getTenDangNhap());
                }
            }

            nhanVienRepository.save(nhanVien);
            return;
        }

        if (nhanVien != null) {
            nhanVien.setTrangThai(false);
            nhanVienRepository.save(nhanVien);
        }
    }

    @Override
    public void delete(Integer id) {
        taiKhoanRepository.deleteById(id);
    }
}
