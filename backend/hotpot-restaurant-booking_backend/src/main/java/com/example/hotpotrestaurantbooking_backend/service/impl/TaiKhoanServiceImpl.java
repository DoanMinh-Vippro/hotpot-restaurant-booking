    package org.example.datlich.impl;

    import org.example.datlich.dto.DTOTaiKhoanRequest;
    import org.example.datlich.dto.DTOTaiKhoanResponse;
    import org.example.datlich.entity.TaiKhoan;
    import org.example.datlich.repository.TaiKhoanRespository;
    import org.example.datlich.service.TaiKhoanService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    public class TaiKhoanServiceImpl implements TaiKhoanService {
        @Autowired
        private TaiKhoanRespository repository;

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
        public DTOTaiKhoanResponse getById(Integer id) {
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
    }
