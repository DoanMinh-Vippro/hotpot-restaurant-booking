package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTONhanVienResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChucVu;
import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.ChucVuRepository;
import com.example.hotpotrestaurantbooking_backend.repository.NhanVienRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRespository;
import com.example.hotpotrestaurantbooking_backend.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    private NhanVien toEntity(DTONhanVienRequest req){
        ChucVu chucVu = chucVuRepo.findById(req.getIdChucVu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));

        TaiKhoan taiKhoan = null;
        if (req.getIdTaiKhoan() != null) {
            taiKhoan = taiKhoanRepo.findById(req.getIdTaiKhoan())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        } else if (req.getTenDangNhap() != null && req.getMatKhau() != null) {
            if (taiKhoanRepo.existsByTenDangNhap(req.getTenDangNhap())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại");
            }
            TaiKhoan newAccount = new TaiKhoan();
            newAccount.setTenDangNhap(req.getTenDangNhap());
            newAccount.setMatKhau(passwordEncoder.encode(req.getMatKhau()));
            newAccount.setTrangThai(true);
            newAccount.setChucVu(chucVu);
            taiKhoanRepo.save(newAccount);
            newAccount.setMaTaiKhoan(String.format("TK%03d", newAccount.getIdTaiKhoan()));
            taiKhoanRepo.save(newAccount);
            taiKhoan = newAccount;
        } else {
            throw new RuntimeException("Cần chọn tài khoản hoặc nhập thông tin đăng nhập để tạo tài khoản mới");
        }

        return NhanVien.builder()
                .maNhanVien(req.getMaNhanVien())
                .tenNhanVien(req.getTenNhanVien())
                .gioiTinh(req.getGioiTinh())
                .soDienThoai(req.getSoDienThoai())
                .email(req.getEmail())
                .diaChi(req.getDiaChi())
                .trangThai(req.getTrangThai())
                .chucVu(chucVu)
                .taiKhoan(taiKhoan)
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

        if (request.getIdTaiKhoan() != null) {
            old.setTaiKhoan(
                    taiKhoanRepo.findById(request.getIdTaiKhoan())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"))
            );
        } else if (old.getTaiKhoan() == null && request.getTenDangNhap() != null && request.getMatKhau() != null) {
            if (taiKhoanRepo.existsByTenDangNhap(request.getTenDangNhap())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại");
            }
            TaiKhoan newAccount = new TaiKhoan();
            newAccount.setTenDangNhap(request.getTenDangNhap());
            newAccount.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
            newAccount.setTrangThai(true);
            newAccount.setChucVu(old.getChucVu());
            taiKhoanRepo.save(newAccount);
            newAccount.setMaTaiKhoan(String.format("TK%03d", newAccount.getIdTaiKhoan()));
            taiKhoanRepo.save(newAccount);
            old.setTaiKhoan(newAccount);
        } else if (old.getTaiKhoan() != null) {
            TaiKhoan currentAccount = old.getTaiKhoan();
            if (request.getTenDangNhap() != null)
                currentAccount.setTenDangNhap(request.getTenDangNhap());
            if (request.getMatKhau() != null)
                currentAccount.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
            taiKhoanRepo.save(currentAccount);
        }

        return toResponse(repository.save(old));
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
