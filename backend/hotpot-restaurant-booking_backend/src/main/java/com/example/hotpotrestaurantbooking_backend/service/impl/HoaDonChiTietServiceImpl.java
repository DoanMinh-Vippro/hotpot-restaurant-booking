package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOHoaDonChiTietResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDonChiTiet;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonChiTietRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonChiTietService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final MonRepository monRepository;
    private final ComboRepository comboRepository;
    private final ChiTietComboRepository chiTietComboRepository;
    private final ModelMapper mapper;

    @Override
    public List<DTOHoaDonChiTietResponse> getAll() {
        return hoaDonChiTietRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonChiTietResponse findById(Integer id) {
        return hoaDonChiTietRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don chi tiet voi id: " + id));
    }

    @Override
    public List<DTOHoaDonChiTietResponse> getChiTietByHoaDonId(Integer idHoaDon) {
        return hoaDonChiTietRepository.findByHoaDon_IdHoaDon(idHoaDon)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public DTOHoaDonChiTietResponse add(DTOHoaDonChiTietRequest request) {
        HoaDonChiTiet hdct = new HoaDonChiTiet();
        updateEntityFromRequest(hdct, request);
        hoaDonChiTietRepository.save(hdct);
        return convertToResponse(hdct);
    }

    @Override
    public DTOHoaDonChiTietResponse update(Integer id, DTOHoaDonChiTietRequest request) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don chi tiet voi id: " + id));
        updateEntityFromRequest(hdct, request);
        hoaDonChiTietRepository.save(hdct);
        return convertToResponse(hdct);
    }

    @Override
    public void delete(Integer id) {
        hoaDonChiTietRepository.deleteById(id);
    }

    private void updateEntityFromRequest(HoaDonChiTiet hdct, DTOHoaDonChiTietRequest request) {
        if (request.getMaHoaDonChiTiet() != null) hdct.setMaHoaDonChiTiet(request.getMaHoaDonChiTiet());
        if (request.getSoLuong() != null) hdct.setSoLuong(request.getSoLuong());
        if (request.getGiaBanTaiThoiDiem() != null) hdct.setGiaBanTaiThoiDien(request.getGiaBanTaiThoiDiem());
        if (request.getTienGiamGiaMon() != null) hdct.setTienGiamGiaMon(request.getTienGiamGiaMon());
        if (request.getThanhTien() != null) hdct.setThanhTien(request.getThanhTien());

        if (request.getIdMon() != null) {
            Mon mon = monRepository.findById(request.getIdMon())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay mon"));
            hdct.setMon(mon);
        }

        if (request.getIdCombo() != null) {
            Combo combo = comboRepository.findById(request.getIdCombo())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay combo"));
            hdct.setCombo(combo);
        }

        if (request.getIdHoaDon() != null) {
            HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay hoa don"));
            hdct.setHoaDon(hoaDon);
        }
    }

    private DTOHoaDonChiTietResponse convertToResponse(HoaDonChiTiet chiTiet) {
        DTOHoaDonChiTietResponse dto = mapper.map(chiTiet, DTOHoaDonChiTietResponse.class);
        if (chiTiet.getMon() != null) {
            dto.setIdMon(chiTiet.getMon().getIdMon());
            dto.setTenMon(chiTiet.getMon().getTenMon());
        }
        if (chiTiet.getCombo() != null) {
            dto.setIdCombo(chiTiet.getCombo().getIdCombo());
            dto.setTenCombo(chiTiet.getCombo().getTenCombo());
            dto.setComboItems(chiTietComboRepository.findMonNamesByComboId(chiTiet.getCombo().getIdCombo()));
        }
        if (chiTiet.getHoaDon() != null) {
            dto.setIdHoaDon(chiTiet.getHoaDon().getIdHoaDon());
        }
        dto.setGiaBanTaiThoiDiem(chiTiet.getGiaBanTaiThoiDien());
        return dto;
    }
}
