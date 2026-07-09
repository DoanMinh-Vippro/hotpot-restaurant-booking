package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOChiTietDatBanComboRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOChiTietDatBanComboResponse;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietDatBanCombo;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietDatBanComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanServiceImpl implements DatBanService {
    private final DatBanRepository datBanRepository;
    private final ModelMapper mapper;
    private final ComboRepository comboRepository;
    private final ChiTietDatBanComboRepository chiTietDatBanComboRepository;
    private final KhachHangRepository khachHangRepository;

    private static final String COMBO_NULL_MSG = "Đơn đặt bàn này không chọn combo đặt trước";

    private void setComboInfo(DatBan db, DTODatBanResponse res) {
        List<DTOChiTietDatBanComboResponse> danhSachCombo = List.of();

        try {
            danhSachCombo = chiTietDatBanComboRepository.findByDatBan_IdDatBan(db.getIdDatBan())
                    .stream()
                    .map(ct -> new DTOChiTietDatBanComboResponse(
                            ct.getCombo() != null ? ct.getCombo().getIdCombo() : null,
                            ct.getCombo() != null ? ct.getCombo().getTenCombo() : null,
                            ct.getCombo() != null ? ct.getCombo().getGiaCombo() : null,
                            ct.getSoLuong()
                    ))
                    .toList();
        } catch (DataAccessException ex) {
            danhSachCombo = List.of();
        }

        res.setDsCombo(danhSachCombo);
    }

    @Override
    public List<DTODatBanResponse> getAll() {
        return datBanRepository.findAll().stream().map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            return res;
        }).toList();
    }

    @Override
    public DTODatBanResponse findById(Integer id) {
        return datBanRepository.findById(id).map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            return res;
        }).orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay don dat ban"));
    }

    @Override
    public DTODatBanResponse add(DTODatBanRequest datBan) {

        DatBan d = mapper.map(datBan, DatBan.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Integer idTaiKhoan = ((Long) jwt.getClaim("idTaiKhoan")).intValue();

        KhachHang khachHang = khachHangRepository
                .findByTaiKhoan_IdTaiKhoan(idTaiKhoan)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));

        d.setIdDatBan(null);

        // Tránh ModelMapper map nhầm Ban cũ gây lỗi FK id_ban khi insert
        d.setBan(null);

        d.setKhachHang(khachHang);

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());

        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);

        if (d.getSoTienCoc() == null) {
            d.setSoTienCoc(BigDecimal.ZERO);
        }

        // ============================================================
        // LƯU ĐẶT BÀN TRƯỚC
        //
        // Phải save DatBan trước để sinh id_dat_ban.
        // Sau đó mới có thể lưu danh sách combo vào bảng trung gian
        // ChiTietDatBanCombo.
        //
        // Thiết kế mới:
        //
        // DatBan
        //      |
        //      | 1 - N
        //      |
        // ChiTietDatBanCombo
        //      |
        //      | N - 1
        //      |
        // Combo
        //
        // Mục tiêu:
        // - Một đơn đặt bàn chọn nhiều combo.
        // - Mỗi combo có số lượng riêng.
        // - Đồng nhất mô hình với HoaDon - HoaDonChiTiet.
        // ============================================================

        datBanRepository.save(d);

        // ============================================================
        // Lưu danh sách combo khách đã chọn.
        //
        // Không dùng ModelMapper vì dsCombo chỉ là DTO.
        // Chủ động lấy Combo từ DB để:
        // - Kiểm tra combo tồn tại.
        // - Lưu đúng số lượng.
        // - Tránh các lỗi mapping ngoài ý muốn.
        // ============================================================

        if (datBan.getDsCombo() != null && !datBan.getDsCombo().isEmpty()) {

            for (DTOChiTietDatBanComboRequest item: datBan.getDsCombo()) {

                Combo combo = comboRepository.findById(item.getIdCombo())
                        .orElseThrow(() ->
                                new CustomResourceNotFoundException("Combo không tồn tại"));

                ChiTietDatBanCombo chiTiet = new ChiTietDatBanCombo();

                chiTiet.setDatBan(d);
                chiTiet.setCombo(combo);
                chiTiet.setSoLuong(item.getSoLuong());

                chiTietDatBanComboRepository.save(chiTiet);
            }

        } else {
            // Không chọn combo thì không cần tiền cọc
            d.setSoTienCoc(BigDecimal.ZERO);
            datBanRepository.save(d);
        }

        DTODatBanResponse res = mapper.map(d, DTODatBanResponse.class);

        setComboInfo(d, res);

        return res;
    }

    @Override
    public DTODatBanResponse update(Integer id, DTODatBanRequest datBan) {
        return datBanRepository.findById(id).map(db -> {

            if (datBan.getSdtKhachHang() != null && !datBan.getSdtKhachHang().isBlank())
                db.setSdtKhachHang(datBan.getSdtKhachHang());

            if (datBan.getSoNguoi() != null)
                db.setSoNguoi(datBan.getSoNguoi());

            if (datBan.getThoiGianDenDuKien() != null)
                db.setThoiGianDenDuKien(datBan.getThoiGianDenDuKien());

            if (datBan.getSoTienCoc() != null)
                db.setSoTienCoc(datBan.getSoTienCoc());

            if (datBan.getPhuongThucThanhToan() != null)
                db.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan());

            if (datBan.getGhiChu() != null)
                db.setGhiChu(datBan.getGhiChu());

            datBanRepository.save(db);

            // Xóa toàn bộ combo cũ của đơn đặt bàn
            chiTietDatBanComboRepository.deleteByDatBan_IdDatBan(db.getIdDatBan());

            // Thêm lại danh sách combo mới
            if (datBan.getDsCombo() != null && !datBan.getDsCombo().isEmpty()) {

                // Duyệt toàn bộ danh sách combo FE gửi lên để lưu lại bảng ChiTietDatBanCombo
                for (DTOChiTietDatBanComboRequest item : datBan.getDsCombo()) {

                    Combo combo = comboRepository.findById(item.getIdCombo())
                            .orElseThrow(() ->
                                    new CustomResourceNotFoundException("Combo không tồn tại"));

                    ChiTietDatBanCombo ct = new ChiTietDatBanCombo();
                    ct.setDatBan(db);
                    ct.setCombo(combo);
                    ct.setSoLuong(item.getSoLuong());

                    chiTietDatBanComboRepository.save(ct);
                }
            }

            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            return res;

        }).orElseThrow(() ->
                new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }

    @Override
    public List<DTODatBanResponse> getDatBanByKhachHang(Integer id) {
        return datBanRepository.findByKhachHang_IdKhachHang(id).stream().map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            return res;
        }).toList();
    }


    @Override
    public DatBan createBookingAfterPayment(Integer idKhachHang, DTODatBanRequest datBan) {

        DatBan d = mapper.map(datBan, DatBan.class);

        KhachHang khachHang = khachHangRepository
                .findById(idKhachHang)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));

        d.setIdDatBan(null);
        d.setBan(null);
        d.setKhachHang(khachHang);

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());

        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);

        if (d.getSoTienCoc() == null) {
            d.setSoTienCoc(BigDecimal.ZERO);
        }

        // Lưu đơn đặt bàn trước để có id_dat_ban
        datBanRepository.save(d);

        // Sau khi DatBan đã được lưu mới có thể lưu các combo vào bảng trung gian
        if (datBan.getDsCombo() != null && !datBan.getDsCombo().isEmpty()) {

            // Duyệt toàn bộ danh sách combo khách hàng gửi lên để lưu từng combo vào bảng ChiTietDatBanCombo
            for (DTOChiTietDatBanComboRequest item : datBan.getDsCombo()) {

                Combo combo = comboRepository.findById(item.getIdCombo())
                        .orElseThrow(() ->
                                new CustomResourceNotFoundException("Combo không tồn tại"));

                ChiTietDatBanCombo ct = new ChiTietDatBanCombo();
                ct.setDatBan(d);
                ct.setCombo(combo);
                ct.setSoLuong(item.getSoLuong());

                chiTietDatBanComboRepository.save(ct);
            }
        }

        return d;
    }


}
