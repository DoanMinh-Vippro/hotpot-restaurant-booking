package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.enums.*;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanServiceImpl implements DatBanService {
    private final DatBanRepository datBanRepository;
    private final ModelMapper mapper;
    private final ComboRepository comboRepository;
    private final ChiTietDatBanComboRepository chiTietDatBanComboRepository;
    private final ChiTietDatBanBanRepository chiTietDatBanBanRepository;
    private final MonRepository monRepository;
    private final ChiTietDatBanMonRepository chiTietDatBanMonRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanRepository banRepository;
    private int tongSucChuaTotNhat;
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
    private void setMonInfo(DatBan db, DTODatBanResponse res) {

        List<DTOChiTietDatBanMonResponse> dsMon = List.of();

        try {
            dsMon = chiTietDatBanMonRepository
                    .findByDatBan_IdDatBan(db.getIdDatBan())
                    .stream()
                    .map(ct -> new DTOChiTietDatBanMonResponse(
                            ct.getIdChiTietDatBanMon(),
                            ct.getMon().getIdMon(),
                            ct.getMon().getTenMon(),
                            ct.getSoLuong()
                    ))
                    .toList();
        } catch (DataAccessException ex) {
            dsMon = List.of();
        }

        res.setDsMon(dsMon);
    }

    private void setBanInfo(DatBan db, DTODatBanResponse res) {
        List<DTOBanResponse> dsBan = new ArrayList<>();

        try {
            dsBan = chiTietDatBanBanRepository.findByDatBan_IdDatBan(db.getIdDatBan())
                    .stream()
                    .filter(ct -> ct.getBan() != null)
                    .map(ct -> {
                        Ban ban = ct.getBan();
                        DTOBanResponse dto = new DTOBanResponse();
                        dto.setIdBan(ban.getIdBan());
                        dto.setTenBan(ban.getTenBan());
                        if (ban.getLoaiBan() != null) {
                            dto.setSucChua(ban.getLoaiBan().getSucChua());
                        }
                        return dto;
                    })
                    .toList();
        } catch (DataAccessException ignored) {
            dsBan = new ArrayList<>();
        }

        res.setDsBan(dsBan);
        if (dsBan.isEmpty()) {
            res.setGhiChu(res.getGhiChu());
            return;
        }

        List<String> tenBanList = dsBan.stream().map(DTOBanResponse::getTenBan).filter(name -> name != null && !name.isBlank()).toList();
        if (!tenBanList.isEmpty()) {
            res.setGhiChu((res.getGhiChu() == null ? "" : res.getGhiChu()) + "");
        }
    }

    private void validateDsBan(List<Integer> dsBan, LocalDateTime thoiGianDenDuKien) {

        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(
                List.of(TrangThaiDatBan.CHO_XAC_NHAN, TrangThaiDatBan.DA_XAC_NHAN)
        );

        for (Integer idBan : dsBan) {
            Ban ban = banRepository.findById(idBan)
                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

            if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
                throw new RuntimeException("Bàn " + ban.getTenBan() + " đang bảo trì.");
            }

            if (banBiTrungLich(ban, thoiGianDenDuKien, dsDatBan)) {
                throw new RuntimeException("Bàn " + ban.getTenBan() + " vừa được khách khác đặt."
                );
            }
        }
    }

    private List<Ban> timDanhSachBanTrong(LocalDateTime thoiGianDenDuKien,
                                          Integer soNguoi) {

        List<Ban> dsBan = banRepository.findAll();

        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(
                List.of(
                        TrangThaiDatBan.CHO_XAC_NHAN,
                        TrangThaiDatBan.DA_XAC_NHAN
                )
        );

        List<Ban> ketQua = new ArrayList<>();

        for (Ban ban : dsBan) {

            // Bàn đang bảo trì
            if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
                continue;
            }

            // Không đủ sức chứa
            if (ban.getLoaiBan().getSucChua() < soNguoi) {
                continue;
            }

            // Bị trùng lịch
            if (banBiTrungLich(ban, thoiGianDenDuKien, dsDatBan)) {
                continue;
            }

            ketQua.add(ban);
        }

        return ketQua;
    }

    private List<Ban> timDanhSachBanKhachCoTheChon(LocalDateTime thoiGianDenDuKien) {
        List<Ban> dsBan = banRepository.findAll();

        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(List.of(TrangThaiDatBan.CHO_XAC_NHAN, TrangThaiDatBan.DA_XAC_NHAN));

        List<Ban> ketQua = new ArrayList<>();

        for (Ban ban : dsBan) {
            if (ban.getLoaiBan() == null) {
                continue;
            }

            if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
                continue;
            }

            if (banBiTrungLich(ban, thoiGianDenDuKien, dsDatBan)) {
                continue;
            }

            ketQua.add(ban);
        }

        ketQua.sort(Comparator.comparing((Ban b) -> b.getKhuVuc() != null ? b.getKhuVuc().getIdKhuVuc() : Integer.MAX_VALUE)
                        .thenComparingInt(b -> b.getLoaiBan().getSucChua())
                        .thenComparing(Ban::getTenBan, Comparator.nullsLast(String::compareTo))
        );

        return ketQua;
    }

    private boolean banBiTrungLich(Ban ban, LocalDateTime thoiGianDenDuKien, List<DatBan> dsDatBan) {
        LocalDateTime ketThucMoi = thoiGianDenDuKien.plusHours(2);

        for (DatBan datBan : dsDatBan) {
            boolean trungBan = datBan.getChiTietDatBanBans()
                    .stream()
                    .anyMatch(ct -> ct.getBan().getIdBan().equals(ban.getIdBan()));

            if (!trungBan) {
                continue;
            }
            LocalDateTime batDauCu = datBan.getThoiGianDenDuKien();
            LocalDateTime ketThucCu = batDauCu.plusHours(2);
            if (thoiGianDenDuKien.isBefore(ketThucCu)
                    && ketThucMoi.isAfter(batDauCu)) {
                return true;
            }
        }

        return false;
    }


    private List<Ban> timBanPhuHop(LocalDateTime thoiGianDenDuKien, Integer soNguoi) {
        List<Ban> dsBanTrong = timDanhSachBanTrong(thoiGianDenDuKien, soNguoi);
        if (dsBanTrong.isEmpty()) {
            return List.of();
        }
        dsBanTrong.sort(Comparator.comparingInt(b -> b.getLoaiBan().getSucChua()));
        return List.of(dsBanTrong.get(0));
    }

    private void timToHopDeQuy(List<Ban> dsBan,
                               Integer soNguoi,
                               int index,
                               List<Ban> ketQua,
                               List<Ban> hienTai,
                               int tongSucChua) {
        // Đã đủ sức chứa
        if (tongSucChua >= soNguoi) {
            if (ketQua.isEmpty()) {
                ketQua.addAll(new ArrayList<>(hienTai));
                tongSucChuaTotNhat = tongSucChua;
            }
            else if (hienTai.size() < ketQua.size()) {
                ketQua.clear();
                ketQua.addAll(new ArrayList<>(hienTai));
                tongSucChuaTotNhat = tongSucChua;
            }
            else if (hienTai.size() == ketQua.size() && tongSucChua < tongSucChuaTotNhat) {
                ketQua.clear();
                ketQua.addAll(new ArrayList<>(hienTai));
                tongSucChuaTotNhat = tongSucChua;
            }
            return;
        }
        // Duyệt hết danh sách
        if (index >= dsBan.size()) {
            return;
        }
        for (int i = index; i < dsBan.size(); i++) {
            Ban ban = dsBan.get(i);
            hienTai.add(ban);
            timToHopDeQuy(dsBan, soNguoi, i + 1, ketQua, hienTai, tongSucChua + ban.getLoaiBan().getSucChua());
            hienTai.remove(hienTai.size() - 1);
        }
    }

    private List<Ban> timToHopBan(LocalDateTime thoiGianDenDuKien, Integer soNguoi) {

        List<Ban> dsBan = timDanhSachBanTrong(
                thoiGianDenDuKien,
                1
        );

        List<Ban> ketQua = new ArrayList<>();

        tongSucChuaTotNhat = Integer.MAX_VALUE;
        dsBan.sort(Comparator
                        .comparing((Ban b) -> b.getKhuVuc().getIdKhuVuc())
                        .thenComparingInt(b -> b.getLoaiBan().getSucChua()));

        timToHopDeQuy(dsBan, soNguoi, 0, ketQua, new ArrayList<>(), 0);
        return ketQua;
    }


    private List<DTOBanResponse> convertBanResponse(List<Ban> dsBan) {
        return dsBan.stream()
                .map(ban -> new DTOBanResponse(
                        ban.getIdBan(),
                        ban.getTenBan(),
                        ban.getLoaiBan(),
                        ban.getLoaiBan().getSucChua(),
                        ban.getKhuVuc() != null ? ban.getKhuVuc().getIdKhuVuc() : null,
                        ban.getKhuVuc() != null ? ban.getKhuVuc().getTenKhuVuc() : null,
                        ban.getTrangThai()
                ))
                .toList();
    }

    private void validateThoiGianHoatDong(LocalDateTime thoiGianDenDuKien) {
        LocalTime gio = thoiGianDenDuKien.toLocalTime();
        boolean caTrua = !gio.isBefore(LocalTime.of(10, 0)) && gio.isBefore(LocalTime.of(14, 0));
        boolean caToi = !gio.isBefore(LocalTime.of(18, 0));
        if (!caTrua && !caToi) {
            throw new RuntimeException("Nhà hàng chỉ nhận đặt bàn từ 10:00-14:00 và 18:00-24:00.");
        }
    }
        //==========================================================================================

    @Override
    public List<DTODatBanResponse> getAll() {
        return datBanRepository.findAll().stream().map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            setMonInfo(db, res);
            setBanInfo(db, res);
            return res;
        }).toList();
    }

    @Override
    public DTODatBanResponse findById(Integer id) {
        return datBanRepository.findById(id).map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            setMonInfo(db, res);
            setBanInfo(db, res);
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

        d.setKhachHang(khachHang);

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());

        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);

        if (d.getSoTienCoc().compareTo(BigDecimal.ZERO) == 0) {
            d.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);
            d.setPhuongThucThanhToan(PhuongThucThanhToan.CHUA_THANH_TOAN);
        }
        validateThoiGianHoatDong(datBan.getThoiGianDenDuKien());
        validateDsBan(datBan.getDsBan(), datBan.getThoiGianDenDuKien());
        datBanRepository.save(d);
        if(datBan.getDsBan() != null && !datBan.getDsBan().isEmpty()) {
            for (Integer idBan : datBan.getDsBan()) {
                Ban ban = banRepository.findById(idBan)
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

                ChiTietDatBanBan chiTietBan = new ChiTietDatBanBan();
                chiTietBan.setDatBan(d);
                chiTietBan.setBan(ban);
                chiTietDatBanBanRepository.save(chiTietBan);
            }
        }

        if (datBan.getDsCombo() != null && !datBan.getDsCombo().isEmpty()) {
            for (DTOChiTietDatBanComboRequest item: datBan.getDsCombo()) {
                Combo combo = comboRepository.findById(item.getIdCombo())
                        .orElseThrow(() -> new CustomResourceNotFoundException("Combo không tồn tại"));

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
        if (datBan.getDsMon() != null && !datBan.getDsMon().isEmpty()) {

            for (DTOChiTietDatBanMonRequest item : datBan.getDsMon()) {

                Mon mon = monRepository.findById(item.getIdMon())
                        .orElseThrow(() ->
                                new CustomResourceNotFoundException("Món không tồn tại"));

                ChiTietDatBanMon ct = new ChiTietDatBanMon();
                ct.setDatBan(d);
                ct.setMon(mon);
                ct.setSoLuong(item.getSoLuong());

                chiTietDatBanMonRepository.save(ct);
            }
        }

        DTODatBanResponse res = mapper.map(d, DTODatBanResponse.class);
        setComboInfo(d, res);
        setMonInfo(d, res);
        setBanInfo(d, res);

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

            chiTietDatBanMonRepository.deleteByDatBan_IdDatBan(db.getIdDatBan());
            if (datBan.getDsMon() != null && !datBan.getDsMon().isEmpty()) {

                for (DTOChiTietDatBanMonRequest item : datBan.getDsMon()) {

                    Mon mon = monRepository.findById(item.getIdMon())
                            .orElseThrow(() ->
                                    new CustomResourceNotFoundException("Món không tồn tại"));

                    ChiTietDatBanMon ct = new ChiTietDatBanMon();
                    ct.setDatBan(db);
                    ct.setMon(mon);
                    ct.setSoLuong(item.getSoLuong());

                    chiTietDatBanMonRepository.save(ct);
                }
            }

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
            setMonInfo(db, res);
            setBanInfo(db, res);
            return res;

        }).orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
    }

    @Override
    public void delete(Integer id) {
        chiTietDatBanMonRepository.deleteByDatBan_IdDatBan(id);
        chiTietDatBanComboRepository.deleteByDatBan_IdDatBan(id);
        chiTietDatBanBanRepository.deleteByDatBan_IdDatBan(id);

        datBanRepository.deleteById(id);
    }

    @Override
    public List<DTODatBanResponse> getDatBanByKhachHang(Integer id) {
        return datBanRepository.findByKhachHang_IdKhachHang(id).stream().map(db -> {
            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            setMonInfo(db, res);
            setBanInfo(db, res);
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
        d.setKhachHang(khachHang);

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());

        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);

        if (d.getSoTienCoc() == null) {
            d.setSoTienCoc(BigDecimal.ZERO);
        }
        validateThoiGianHoatDong(datBan.getThoiGianDenDuKien());
        validateDsBan(datBan.getDsBan(), datBan.getThoiGianDenDuKien());
        // Lưu đơn đặt bàn trước để có id_dat_ban
        datBanRepository.save(d);
        if (datBan.getDsBan() != null && !datBan.getDsBan().isEmpty()) {
            for (Integer idBan : datBan.getDsBan()) {
                Ban ban = banRepository.findById(idBan)
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

                ChiTietDatBanBan chiTietBan = new ChiTietDatBanBan();
                chiTietBan.setDatBan(d);
                chiTietBan.setBan(ban);
                chiTietDatBanBanRepository.save(chiTietBan);
            }
        }

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
        if (datBan.getDsMon() != null && !datBan.getDsMon().isEmpty()) {

            for (DTOChiTietDatBanMonRequest item : datBan.getDsMon()) {

                Mon mon = monRepository.findById(item.getIdMon())
                        .orElseThrow(() ->
                                new CustomResourceNotFoundException("Món không tồn tại"));

                ChiTietDatBanMon ct = new ChiTietDatBanMon();
                ct.setDatBan(d);
                ct.setMon(mon);
                ct.setSoLuong(item.getSoLuong());

                chiTietDatBanMonRepository.save(ct);
            }
        }

        return d;
    }


    @Override
    public DTOCheckBanResponse checkBan(DTOCheckBanRequest request) {

        DTOCheckBanResponse response = new DTOCheckBanResponse();

        /*
         * ============================================================
         * 1. LẤY TOÀN BỘ BÀN ĐANG TRỐNG
         *
         * Danh sách này dùng cho khách tự chọn bàn.
         * Không lọc theo số người ở đây.
         *
         * Ví dụ:
         * Khách đặt 2 người
         * B1 = 2 chỗ
         * B2 = 4 chỗ
         * B3 = 6 chỗ
         *
         * => dsBanTrong = B1, B2, B3
         *
         * Sau đó FE tự đánh dấu bàn không đủ sức chứa.
         * ============================================================
         */

        List<Ban> dsBanTrong =
                timDanhSachBanKhachCoTheChon(
                        request.getThoiGianDenDuKien()
                );

        /*
         * ============================================================
         * 2. TÌM BÀN ĐƠN PHÙ HỢP NHẤT
         *
         * Đây là bàn hệ thống đề xuất.
         * Ví dụ khách 2 người:
         * B1 = 2 chỗ
         * B2 = 4 chỗ
         *
         * => dsBanDeXuat = B1
         * ============================================================
         */

        List<Ban> dsBanDon =
                timBanPhuHop(
                        request.getThoiGianDenDuKien(),
                        request.getSoNguoi()
                );

        if (!dsBanDon.isEmpty()) {

            response.setTrangThai(
                    TrangThaiCheckBan.CO_BAN_DON.name()
            );

            response.setMessage(
                    "Nhà hàng còn bàn phù hợp"
            );

            response.setCanGhep(false);

            response.setTongSucChua(
                    dsBanDon.stream()
                            .mapToInt(b -> b.getLoaiBan().getSucChua())
                            .sum()
            );

            /*
             * Bàn hệ thống đề xuất
             */
            response.setDsBan(
                    convertBanResponse(dsBanDon)
            );

            /*
             * TOÀN BỘ BÀN khách có thể xem/chọn
             */
            response.setDsBanTrong(
                    convertBanResponse(dsBanTrong)
            );

            return response;
        }

        /*
         * ============================================================
         * 3. KHÔNG CÓ BÀN ĐƠN -> THỬ GHÉP BÀN
         * ============================================================
         */

        List<Ban> dsBanGhep =
                timToHopBan(
                        request.getThoiGianDenDuKien(),
                        request.getSoNguoi()
                );

        if (!dsBanGhep.isEmpty()) {

            response.setTrangThai(
                    TrangThaiCheckBan.CAN_GHEP.name()
            );

            response.setMessage(
                    "Cần ghép nhiều bàn để phục vụ"
            );

            response.setCanGhep(true);

            response.setTongSucChua(
                    dsBanGhep.stream()
                            .mapToInt(b -> b.getLoaiBan().getSucChua())
                            .sum()
            );

            /*
             * Tổ hợp bàn hệ thống đề xuất
             */
            response.setDsBan(
                    convertBanResponse(dsBanGhep)
            );

            /*
             * TOÀN BỘ BÀN TRỐNG cho khách tự chọn
             */
            response.setDsBanTrong(
                    convertBanResponse(dsBanTrong)
            );

            return response;
        }

        /*
         * ============================================================
         * 4. KHÔNG CÓ BÀN PHÙ HỢP
         * ============================================================
         */

        response.setTrangThai(
                TrangThaiCheckBan.KHONG_CO_BAN.name()
        );

        response.setMessage(
                "Nhà hàng hiện không đủ sức chứa"
        );

        response.setCanGhep(false);

        response.setTongSucChua(
                dsBanTrong.stream()
                        .mapToInt(b -> b.getLoaiBan().getSucChua())
                        .sum()
        );

        /*
         * Không có bàn đơn/tổ hợp phù hợp.
         */
        response.setDsBan(List.of());

        /*
         * Vẫn trả toàn bộ bàn đang trống.
         *
         * Trường hợp này rất hữu ích để FE biết:
         * "Nhà hàng vẫn còn bàn nhưng không có bàn/tổ hợp
         * nào đáp ứng đủ số người."
         */
        response.setDsBanTrong(
                convertBanResponse(dsBanTrong)
        );

        return response;
    }

    private List<Ban> timTatCaBanTrong(LocalDateTime thoiGianDenDuKien) {
        List<Ban> dsBan = banRepository.findAll();
        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(List.of(TrangThaiDatBan.CHO_XAC_NHAN, TrangThaiDatBan.DA_XAC_NHAN));
        List<Ban> ketQua = new ArrayList<>();
        for (Ban ban : dsBan) {
            // Bàn đang bảo trì
            if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
                continue;
            }
            // Bàn bị trùng lịch
            if (banBiTrungLich(ban, thoiGianDenDuKien, dsDatBan)) {
                continue;
            }
            ketQua.add(ban);
        }
        return ketQua;
    }

    @Override
    public DTOTinhTrangBanResponse tinhTrangBan(LocalDateTime thoiGianDenDuKien) {

        validateThoiGianHoatDong(thoiGianDenDuKien);

        List<Ban> dsBan = banRepository.findAll();

        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(
                List.of(
                        TrangThaiDatBan.CHO_XAC_NHAN,
                        TrangThaiDatBan.DA_XAC_NHAN
                )
        );

        List<Ban> dsBanTrong = new ArrayList<>();

        for (Ban ban : dsBan) {

            if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
                continue;
            }

            if (banBiTrungLich(ban, thoiGianDenDuKien, dsDatBan)) {
                continue;
            }

            dsBanTrong.add(ban);
        }

        int soBanConLai = dsBanTrong.size();

        int tongSucChua = dsBanTrong.stream()
                .mapToInt(ban -> ban.getLoaiBan().getSucChua())
                .sum();

        DTOTinhTrangBanResponse response = new DTOTinhTrangBanResponse();

        response.setSoBanConLai(soBanConLai);
        response.setTongSucChua(tongSucChua);

        return response;
    }

    @Override
    public List<DTOBanResponse> getDanhSachBanCoTheChon(LocalDateTime thoiGianDenDuKien) {
        validateThoiGianHoatDong(thoiGianDenDuKien);
        List<Ban> dsBan = timDanhSachBanKhachCoTheChon(thoiGianDenDuKien);
        return convertBanResponse(dsBan);
    }


}
