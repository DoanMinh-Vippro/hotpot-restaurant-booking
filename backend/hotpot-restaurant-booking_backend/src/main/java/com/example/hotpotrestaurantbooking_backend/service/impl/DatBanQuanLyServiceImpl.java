package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanQuanLyServiceImpl implements DatBanQuanLyService {
    private final ModelMapper mapper;
    private final DatBanRepository datBanRepository;
    private KhachHang kh;
    private Ban b;

    @Override
    public List<DTODatBanQuanLyResponse> getAll() {
        return datBanRepository
                .findAll()
                .stream()
                .map(d -> {
                    DTODatBanQuanLyResponse response = mapper.map(d, DTODatBanQuanLyResponse.class);
                    response.setLoaiBan(d.getBan().getLoaiBan());
                    response.setTenKhachHang(d.getKhachHang().getTenKhachHang());
                    return response;
                })
                .toList();
    }

    @Override
    public DTODatBanQuanLyResponse findById(Integer id) {
        return datBanRepository
                .findById(id)
                .map(d -> {
                    DTODatBanQuanLyResponse response = mapper.map(d, DTODatBanQuanLyResponse.class);
                    response.setLoaiBan(d.getBan().getLoaiBan());
                    response.setTenKhachHang(d.getKhachHang().getTenKhachHang());
                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d) {
        DatBan db = mapper.map(d, DatBan.class);
        datBanRepository.save(db);
        return mapper.map(db, DTODatBanQuanLyResponse.class);
    }

    @Override
    public DTODatBanQuanLyResponse update(Integer id, DTODatBanQuanLyRequest d) {
        return datBanRepository
                .findById(id)
                .map(db -> {
                    if (d.getIdBan() != null) db.setBan(db.getBan());

                    if (d.getIdkhachHang() != null) db.setKhachHang(db.getKhachHang());

                    if (d.getSdtKhachHang() != null && !d.getSdtKhachHang().isBlank()) db.setSdtKhachHang(d.getSdtKhachHang());

                    if (d.getSoNguoi() > 0) db.setSoNguoi(d.getSoNguoi());

                    if (d.getTrangThai() != null) db.setTrangThai(d.getTrangThai());

                    if (d.getGhiChu() != null) db.setGhiChu(d.getGhiChu());

                    if (d.getThoiGianDenDuKien() != null) db.setThoiGianDenDuKien(d.getThoiGianDenDuKien());

                    if (d.getSoTienCoc() != null) db.setSoTienCoc(d.getSoTienCoc());

                    if (d.getTrangThaiCoc() != null) db.setTrangThaiCoc(d.getTrangThaiCoc());

                    if (d.getPhuongThucThanhToan() != null) db.setPhuongThucThanhToan(d.getPhuongThucThanhToan());
                    DTODatBanQuanLyResponse response = mapper.map(db, DTODatBanQuanLyResponse.class);
                    response.setLoaiBan(db.getBan().getLoaiBan());
                    response.setTenKhachHang(db.getKhachHang().getTenKhachHang());
                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }
}
