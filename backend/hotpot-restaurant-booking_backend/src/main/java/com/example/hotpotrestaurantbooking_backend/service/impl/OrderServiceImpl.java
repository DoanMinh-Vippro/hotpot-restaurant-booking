package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDonChiTiet;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.OrderSevice;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.example.hotpotrestaurantbooking_backend.dto.DTOKhuVucOrderResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import org.springframework.transaction.annotation.Transactional;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderSevice {
    private final BanRepository banRepository;
    private final ModelMapper mapper;
    private final HoaDonRepository hoaDonRepository;
    private final MonRepository monRepository;
    private final ComboRepository comboRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    @Override
    public List<DTOKhuVucOrderResponse> getDanhSachBanOrder() {

        List<Ban> dsBan = banRepository.findByTrangThaiInOrderByKhuVuc_IdKhuVucAscTenBanAsc(
                        List.of(TrangThaiBan.DA_DAT, TrangThaiBan.DANG_SU_DUNG));

        Map<Integer, List<Ban>> group = dsBan.stream()
                .collect(Collectors.groupingBy(b -> b.getKhuVuc().getIdKhuVuc()));

        List<DTOKhuVucOrderResponse> result = new ArrayList<>();

        for (List<Ban> bans : group.values()) {
            Ban first = bans.get(0);
            DTOKhuVucOrderResponse khuVuc = new DTOKhuVucOrderResponse();
            khuVuc.setIdKhuVuc(first.getKhuVuc().getIdKhuVuc());
            khuVuc.setTenKhuVuc(first.getKhuVuc().getTenKhuVuc());
            khuVuc.setDsBan(
                    bans.stream()
                            .map(ban -> {
                                DTOBanResponse dto = mapper.map(ban, DTOBanResponse.class);
                                dto.setSucChua(ban.getLoaiBan().getSucChua());
                                return dto;
                            })
                            .toList()
            );

            result.add(khuVuc);
        }

        return result;
    }

    @Override
    public DTOOrderHoaDonResponse chonBan(Integer idBan) {
        HoaDon hoaDon = hoaDonRepository
                .findByBan_IdBanAndTrangThaiHoaDon(idBan, 0)
                .orElseThrow(() -> new RuntimeException("Bàn này chưa có hóa đơn đang hoạt động."));
        return mapper.map(hoaDon, DTOOrderHoaDonResponse.class);
    }

    @Override
    public DTOOrderMenuResponse getMenu() {
        DTOOrderMenuResponse dto = new DTOOrderMenuResponse();
        dto.setDsMon(monRepository.findAll()
                        .stream()
                        .map(mon -> mapper.map(mon, MonResponse.class))
                        .toList()
        );

        dto.setDsCombo(comboRepository.findAll()
                        .stream()
                        .map(combo -> mapper.map(combo, ComboResponse.class))
                        .toList()
        );

        return dto;
    }

    @Override
    @Transactional
    public void themMon(DTOOrderThemMonRequest request) {

        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng không hợp lệ.");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        HoaDonChiTiet ct = hoaDonChiTietRepository
                .findByHoaDon_IdHoaDonAndMon_IdMon(
                        request.getIdHoaDon(),
                        request.getIdMon())
                .orElse(null);

        Mon mon = monRepository.findById(request.getIdMon()).orElseThrow(() -> new RuntimeException("Không tìm thấy món."));

        if (ct != null) {
            ct.setSoLuong(ct.getSoLuong() + request.getSoLuong());
            ct.setThanhTien(ct.getGiaBanTaiThoiDien().multiply(BigDecimal.valueOf(ct.getSoLuong())));
            hoaDonChiTietRepository.save(ct);

        } else {
            HoaDonChiTiet newCt = new HoaDonChiTiet();
            newCt.setHoaDon(hoaDon);
            newCt.setMon(mon);
            newCt.setSoLuong(request.getSoLuong());
            newCt.setGiaBanTaiThoiDien(mon.getDonGiaHienTai());
            newCt.setTienGiamGiaMon(BigDecimal.ZERO);
            newCt.setThanhTien(mon.getDonGiaHienTai().multiply(BigDecimal.valueOf(request.getSoLuong())));
            hoaDonChiTietRepository.save(newCt);
        }

        tinhLaiHoaDon(hoaDon);
    }

    @Override
    @Transactional
    public void themCombo(DTOOrderThemComboRequest request) {
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng không hợp lệ.");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        HoaDonChiTiet ct = hoaDonChiTietRepository
                .findByHoaDon_IdHoaDonAndCombo_IdCombo(
                        request.getIdHoaDon(),
                        request.getIdCombo())
                .orElse(null);

        Combo combo = comboRepository.findById(request.getIdCombo())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy combo."));

        if (ct != null) {
            ct.setSoLuong(ct.getSoLuong() + request.getSoLuong());
            ct.setThanhTien(ct.getGiaBanTaiThoiDien().multiply(BigDecimal.valueOf(ct.getSoLuong())));
            hoaDonChiTietRepository.save(ct);
        } else {
            HoaDonChiTiet newCt = new HoaDonChiTiet();
            newCt.setHoaDon(hoaDon);
            newCt.setCombo(combo);
            newCt.setSoLuong(request.getSoLuong());
            newCt.setGiaBanTaiThoiDien(combo.getGiaCombo());
            newCt.setTienGiamGiaMon(BigDecimal.ZERO);
            newCt.setThanhTien(combo.getGiaCombo().multiply(BigDecimal.valueOf(request.getSoLuong())));
            hoaDonChiTietRepository.save(newCt);
        }

        tinhLaiHoaDon(hoaDon);
    }

    @Override
    public DTOOrderHoaDonChiTietResponse getChiTietHoaDon(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        List<HoaDonChiTiet> ds = hoaDonChiTietRepository.findByHoaDon_IdHoaDon(idHoaDon);
        DTOOrderHoaDonChiTietResponse response = new DTOOrderHoaDonChiTietResponse();
        response.setIdHoaDon(idHoaDon);
        response.setTongTien(hoaDon.getTongTien());
        for (HoaDonChiTiet ct : ds) {
            DTOOrderItemResponse item = new DTOOrderItemResponse();
            item.setIdHoaDonChiTiet(ct.getIdHoaDonChiTiet());
            item.setSoLuong(ct.getSoLuong());
            item.setDonGia(ct.getGiaBanTaiThoiDien());
            item.setThanhTien(ct.getThanhTien());
            if (ct.getMon() != null) {
                item.setIdMon(ct.getMon().getIdMon());
                item.setTenMon(ct.getMon().getTenMon());
                item.setCombo(false);
                response.getDsMon().add(item);
            } else if (ct.getCombo() != null) {
                item.setIdCombo(ct.getCombo().getIdCombo());
                item.setTenCombo(ct.getCombo().getTenCombo());
                item.setCombo(true);
                response.getDsCombo().add(item);
            }
        }
        return response;
    }


    private void tinhLaiHoaDon(HoaDon hoaDon) {
        List<HoaDonChiTiet> ds = hoaDonChiTietRepository.findByHoaDon_IdHoaDon(hoaDon.getIdHoaDon());
        BigDecimal tong = ds.stream().map(HoaDonChiTiet::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        hoaDon.setTienTruocGiam(tong);
        BigDecimal giamGia = hoaDon.getTienGiamGia() == null ? BigDecimal.ZERO : hoaDon.getTienGiamGia();
        hoaDon.setTongTien(tong.subtract(giamGia));
        hoaDonRepository.save(hoaDon);
    }
}
