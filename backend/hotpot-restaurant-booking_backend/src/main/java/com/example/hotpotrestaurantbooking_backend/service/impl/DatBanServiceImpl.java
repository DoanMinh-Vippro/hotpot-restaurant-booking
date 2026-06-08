package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanServiceImpl implements DatBanService {
    private final DatBanRepository datBanRepository;
    private final ModelMapper mapper;
    @Override
    public List<DTODatBanResponse> getAll() {
        return datBanRepository
                .findAll()
                .stream()
                .map(db -> mapper.map(db,DTODatBanResponse.class))
                .toList();
    }

    @Override
    public DTODatBanResponse findById(Integer id) {
        return datBanRepository
                .findById(id)
                .map(db -> mapper.map(db, DTODatBanResponse.class))
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public DTODatBanResponse add(DTODatBanRequest datBan) {
        DatBan d = mapper.map(datBan,DatBan.class);
        d.setGioDat(LocalTime.now());
        d.setNgayDat(LocalDate.now());
        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);
        datBanRepository.save(d);
        return mapper.map(d,DTODatBanResponse.class);
    }

    @Override
    public DTODatBanResponse update(Integer id, DTODatBanRequest datBan) {
        return datBanRepository
                .findById(id)
                .map(db -> {
                    if(datBan.getSdtKhachHang() != null && !datBan.getSdtKhachHang().isBlank()) db.setSdtKhachHang(datBan.getSdtKhachHang());
                    if(datBan.getSoNguoi() != null) db.setSoNguoi(datBan.getSoNguoi());
                    if(datBan.getThoiGianDenDuKien() != null) db.setThoiGianDenDuKien(datBan.getThoiGianDenDuKien());
                    if(datBan.getSoTienCoc() != null) db.setSoTienCoc(datBan.getSoTienCoc());
                    if(datBan.getPhuongThucThanhToan() != null) db.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan());
                    datBanRepository.save(db);
                    return mapper.map(db,DTODatBanResponse.class);
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }

    @Override
    public List<DTODatBanResponse> getDatBanByKhachHang(Integer id) {
        List<DatBan> list = datBanRepository.findByKhachHang_IdKhachHang(id);
        return list.stream()
                .map(dbkh -> mapper.map(dbkh,DTODatBanResponse.class))
                .toList();
    }
}
