package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanQuanLyServiceImpl implements DatBanQuanLyService {

    private final ModelMapper mapper;
    private final DatBanRepository datBanRepository;
    private final BanRepository banRepository;
    private final ComboRepository comboRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;

    // =========================
    // MAP RESPONSE (SAFE NULL BAN)
    // =========================
    private DTODatBanQuanLyResponse mapToResponse(DatBan d) {
        DTODatBanQuanLyResponse response = mapper.map(d, DTODatBanQuanLyResponse.class);

        if (d.getBan() != null) {
            response.setIdBan(d.getBan().getIdBan());
        }

        if (d.getKhachHang() != null) {
            response.setTenKhachHang(d.getKhachHang().getTenKhachHang());
            response.setIdKhachHang(d.getKhachHang().getIdKhachHang());
            response.setSdtKhachHang(d.getSdtKhachHang());
        } else {
            response.setTenKhachHang("Khách vãng lai");
        }

        response.setSoTienCoc(d.getSoTienCoc());
        response.setTrangThaiCoc(d.getTrangThaiCoc());
        response.setPhuongThucThanhToan(d.getPhuongThucThanhToan());
        response.setGhiChu(d.getGhiChu());
        response.setNgayDat(d.getNgayDat());
        response.setGioDat(d.getGioDat().toLocalTime());
        response.setThoiGianDenDuKien(d.getThoiGianDenDuKien());
        response.setSoNguoi(d.getSoNguoi());
        response.setTrangThai(d.getTrangThai());

        if (d.getCombo() != null) {
            response.setIdCombo(d.getCombo().getIdCombo());
            response.setTenCombo(d.getCombo().getTenCombo());
            response.setGiaCombo(d.getCombo().getGiaCombo());
        }

        return response;
    }

    @Override
    public List<DTODatBanQuanLyResponse> getAll() {
        return datBanRepository.findAll()
                .stream()
                .filter(this::isVisibleReservation)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DTODatBanQuanLyResponse findById(Integer id) {
        return datBanRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d) {

        DatBan db = mapper.map(d, DatBan.class);

        if (d.getIdkhachHang() != null) {
            KhachHang kh = khachHangRepository.findById(d.getIdkhachHang())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));
            db.setKhachHang(kh);
        } else {
            db.setKhachHang(null);
        }

        // Tạm thời không gán bàn cho đơn đặt bàn walk-in.
        // Nếu client gửi một bàn không hợp lệ hoặc bàn chưa được persist, hãy bỏ tham chiếu để tránh lỗi Hibernate.
        if (d.getIdBan() != null) {
            Ban existingBan = banRepository.findById(d.getIdBan()).orElse(null);
            if (existingBan != null) {
                db.setBan(existingBan);
            } else {
                db.setBan(null);
            }
        } else {
            db.setBan(null);
        }

        if (d.getIdCombo() != null) {
            Combo existingCombo = comboRepository.findById(d.getIdCombo()).orElse(null);
            db.setCombo(existingCombo);
        } else {
            db.setCombo(null);
        }

        db.setGioDat(Time.valueOf(LocalTime.now()));
        db.setNgayDat(LocalDate.now());
        if (db.getSoTienCoc() == null) {
            db.setSoTienCoc(BigDecimal.ZERO);
        }

        datBanRepository.save(db);

        DTODatBanQuanLyResponse response = mapper.map(db, DTODatBanQuanLyResponse.class);

        // response.setLoaiBan(ban.getLoaiBan());
        // response.setIdBan(db.getBan().getIdBan());

        KhachHang linkedCustomer = db.getKhachHang();
        response.setTenKhachHang(linkedCustomer != null ? linkedCustomer.getTenKhachHang() : "Khách vãng lai");
        response.setIdKhachHang(linkedCustomer != null ? linkedCustomer.getIdKhachHang() : null);
        response.setSdtKhachHang(db.getSdtKhachHang());
        response.setTrangThaiCoc(db.getTrangThaiCoc());
        response.setSoTienCoc(db.getSoTienCoc());
        response.setPhuongThucThanhToan(db.getPhuongThucThanhToan());

        return response;
    }

    @Override
    public DTODatBanQuanLyResponse update(Integer id, DTODatBanQuanLyRequest d) {
        return datBanRepository.findById(id)
                .map(db -> {

                    // Tạm thời không gán bàn cho đơn đặt bàn walk-in.
                    // Nếu client gửi một bàn không hợp lệ hoặc bàn chưa được persist, hãy bỏ tham chiếu để tránh lỗi Hibernate.
                    if (d.getIdBan() != null) {
                        Ban existingBan = banRepository.findById(d.getIdBan()).orElse(null);
                        if (existingBan != null) {
                            db.setBan(existingBan);
                        } else {
                            db.setBan(null);
                        }
                    } else {
                        db.setBan(null);
                    }

                    if (d.getIdCombo() != null) {
                        Combo existingCombo = comboRepository.findById(d.getIdCombo()).orElse(null);
                        db.setCombo(existingCombo);
                    } else {
                        db.setCombo(null);
                    }

                    if (d.getIdkhachHang() != null) {
                        KhachHang kh = khachHangRepository.findById(d.getIdkhachHang())
                                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));
                        db.setKhachHang(kh);
                    } else {
                        db.setKhachHang(null);
                    }

                    if (d.getSdtKhachHang() != null && !d.getSdtKhachHang().isBlank())
                        db.setSdtKhachHang(d.getSdtKhachHang());

                    if (d.getSoNguoi() > 0)
                        db.setSoNguoi(d.getSoNguoi());

                    if (d.getTrangThai() != null)
                        db.setTrangThai(d.getTrangThai());

                    if (d.getGhiChu() != null)
                        db.setGhiChu(d.getGhiChu());

                    if (d.getThoiGianDenDuKien() != null)
                        db.setThoiGianDenDuKien(d.getThoiGianDenDuKien());

                    if (d.getSoTienCoc() != null)
                        db.setSoTienCoc(d.getSoTienCoc());
                    else if (db.getSoTienCoc() == null)
                        db.setSoTienCoc(BigDecimal.ZERO);

                    if (d.getTrangThaiCoc() != null)
                        db.setTrangThaiCoc(d.getTrangThaiCoc());

                    if (d.getPhuongThucThanhToan() != null)
                        db.setPhuongThucThanhToan(d.getPhuongThucThanhToan());

                    datBanRepository.save(db);

                    DTODatBanQuanLyResponse response = mapper.map(db, DTODatBanQuanLyResponse.class);

                    // response.setLoaiBan(db.getBan().getLoaiBan());
                    // response.setIdBan(db.getBan().getIdBan());

                    response.setTenKhachHang(db.getKhachHang() != null ? db.getKhachHang().getTenKhachHang() : "Khách vãng lai");
                    response.setIdKhachHang(db.getKhachHang() != null ? db.getKhachHang().getIdKhachHang() : null);
                    response.setSdtKhachHang(db.getSdtKhachHang());
                    response.setTrangThaiCoc(db.getTrangThaiCoc());
                    response.setSoTienCoc(db.getSoTienCoc());
                    response.setPhuongThucThanhToan(db.getPhuongThucThanhToan());

                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }

    @Override
    public List<DTODatBanQuanLyResponse> findByTrangThai(TrangThaiDatBan trangThai) {
        return datBanRepository.findByTrangThai(trangThai)
                .stream()
                .filter(this::isVisibleReservation)
                .map(this::mapToResponse)
                .toList();
    }

    private boolean isVisibleReservation(DatBan datBan) {
        return !hoaDonRepository.existsByDatBan_IdDatBanAndTrangThaiThanhToan(datBan.getIdDatBan(), 1);
    }
}