<<<<<<< HEAD
// ===========================================
// TaiKhoanServiceImpl
// ===========================================
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
=======
    package com.example.hotpotrestaurantbooking_backend.service.impl;

    import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
    import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
    import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
    import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRespository;

    import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
>>>>>>> 6741c3e41b67912050083fa6b80f7e4b3d98e35f

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiKhoanServiceImpl implements TaiKhoanService {
    private final ModelMapper mapper;
    private final TaiKhoanRepository taiKhoanRepository;

<<<<<<< HEAD
    @Override
    public List<DTOTaiKhoanResponse> getAll() {
        return taiKhoanRepository
                .findAll()
                .stream()
                .map(t -> mapper.map(t,DTOTaiKhoanResponse.class))
                .toList();
=======
        private TaiKhoan toEntity(DTOTaiKhoanRequest req){
            return TaiKhoan.builder()
                    .maTaiKhoan(req.getMaTaiKhoan())
                    .tenDangNhap(req.getTenDangNhap())
                    .matKhau(req.getMatKhau())
                    .trangThai(req.getTrangThai())
                    .build();
        }
        private DTOTaiKhoanResponse toResponse(TaiKhoan tk){
            return DTOTaiKhoanResponse.builder()
                    .id(tk.getId())
                    .maTaiKhoan(tk.getMaTaiKhoan())
                    .tenDangNhap(tk.getTenDangNhap())
                    .matKhau(tk.getMatKhau())
                    .trangThai(tk.getTrangThai())
                    .build();
        }
        @Override
        public List<DTOTaiKhoanResponse> getAll() {

            return repository.findAll().stream().map(this::toResponse).toList();
        }

        @Override
        public DTOTaiKhoanResponse findById(Integer id) {
            TaiKhoan tk = repository.findById(id).orElse(null);
            return tk != null ? toResponse(tk) : null;
        }

        @Override
        public DTOTaiKhoanResponse add(DTOTaiKhoanRequest request) {
            TaiKhoan tk = toEntity(request);
            return toResponse(repository.save(tk));
        }

        @Override
        public DTOTaiKhoanResponse update(Integer id, DTOTaiKhoanRequest request) {

            TaiKhoan old = repository.findById(id).orElse(null);

            old.setMaTaiKhoan(request.getMaTaiKhoan());
            old.setTenDangNhap(request.getTenDangNhap());
            old.setMatKhau(request.getMatKhau());
            old.setTrangThai(request.getTrangThai());

            return toResponse(repository.save(old));
        }

        @Override
        public void delete(Integer id) {
            repository.deleteById(id);
        }
>>>>>>> 6741c3e41b67912050083fa6b80f7e4b3d98e35f
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
