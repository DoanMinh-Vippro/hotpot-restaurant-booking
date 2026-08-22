package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.*;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.*;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//mail
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;


@RequiredArgsConstructor
@Service
@Transactional
public class DatBanQuanLyServiceImpl implements DatBanQuanLyService {

    private final ModelMapper mapper;
    private final DatBanRepository datBanRepository;
    private final BanRepository banRepository;
    private final ComboRepository comboRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final MonRepository monRepository;
    private final ChiTietDatBanMonRepository chiTietDatBanMonRepository;
    private final JavaMailSender mailSender; // Tiêm Bean gửi Mail

    private static final long THOI_GIAN_GIU_BAN = 3;

    @PersistenceContext
    private EntityManager entityManager;

    private DTODatBanQuanLyResponse mapToResponse(DatBan d) {
        DTODatBanQuanLyResponse response = mapper.map(d, DTODatBanQuanLyResponse.class);

        // Khách hàng
        KhachHang kh = d.getKhachHang();

        if (kh != null) {
            response.setIdKhachHang(kh.getIdKhachHang());
            response.setTenKhachHang(kh.getTenKhachHang());
        } else {
            response.setIdKhachHang(null);
            response.setTenKhachHang("Khách vãng lai");
        }

        response.setSdtKhachHang(d.getSdtKhachHang());

        // Danh sách bàn
        if (d.getChiTietDatBanBans() != null) {
            response.setDsBan(
                    d.getChiTietDatBanBans()
                            .stream()
                            .filter(ct -> ct.getBan() != null)
                            .map(ct -> {
                                Ban ban = ct.getBan();
                                DTOBanResponse dto = mapper.map(ban, DTOBanResponse.class);
                                dto.setSucChua(ban.getLoaiBan().getSucChua());
                                return dto;
                            })
                            .toList()
            );
        } else {
            response.setDsBan(new ArrayList<>());
        }

        // Danh sách combo
        if (d.getChiTietDatBanCombos() != null) {
            response.setDsCombo(
                    d.getChiTietDatBanCombos()
                            .stream()
                            .filter(ct -> ct.getCombo() != null)
                            .map(ct -> new DTOChiTietDatBanComboResponse(
                                    ct.getCombo().getIdCombo(),
                                    ct.getCombo().getTenCombo(),
                                    ct.getCombo().getGiaCombo(),
                                    ct.getSoLuong()
                            ))
                            .toList()
            );
        } else {
            response.setDsCombo(new ArrayList<>());
        }

        // Danh sách món
        if (d.getChiTietDatBanMons() != null) {
            response.setDsMon(
                    d.getChiTietDatBanMons()
                            .stream()
                            .filter(ct -> ct.getMon() != null)
                            .map(ct -> new DTOChiTietDatBanMonResponse(
                                    ct.getIdChiTietDatBanMon(),
                                    ct.getMon().getIdMon(),
                                    ct.getMon().getTenMon(),
                                    ct.getSoLuong()
                            ))
                            .toList()
            );
        } else {
            response.setDsMon(new ArrayList<>());
        }

        // Thông tin đơn
        response.setNgayDat(d.getNgayDat());

        if (d.getGioDat() != null) {
            response.setGioDat(d.getGioDat());
        }

        response.setSoNguoi(d.getSoNguoi());
        response.setTrangThai(d.getTrangThai());
        response.setGhiChu(d.getGhiChu());
        response.setThoiGianDenDuKien(d.getThoiGianDenDuKien());

        // Cọc
        response.setSoTienCoc(d.getSoTienCoc());
        response.setTrangThaiCoc(d.getTrangThaiCoc());
        response.setPhuongThucThanhToan(d.getPhuongThucThanhToan());

        return response;
    }

    //validate
    private void validateDanhSachBan(List<Integer> dsBan, LocalDateTime thoiGianDenDuKien, Integer idDatBan) {

        if (dsBan == null || dsBan.isEmpty()) {
            return;
        }
        LocalDateTime newStart = thoiGianDenDuKien;
        LocalDateTime newEnd = newStart.plusHours(THOI_GIAN_GIU_BAN);

        List<DatBan> dsDangHoatDong = datBanRepository.findByTrangThaiIn(
                List.of(TrangThaiDatBan.CHO_XAC_NHAN, TrangThaiDatBan.DA_XAC_NHAN, TrangThaiDatBan.DA_NHAN_BAN));

        if (idDatBan != null) {
            dsDangHoatDong.removeIf(db -> db.getIdDatBan().equals(idDatBan));
        }

        for (DatBan datBan: dsDangHoatDong) {
            if (datBan.getChiTietDatBanBans() == null || datBan.getChiTietDatBanBans().isEmpty()) {
                continue;
            }

            LocalDateTime oldStart = datBan.getThoiGianDenDuKien();

            if (oldStart == null) {
                continue;
            }

            LocalDateTime oldEnd = oldStart.plusHours(THOI_GIAN_GIU_BAN);
            boolean trungThoiGian = newStart.isBefore(oldEnd) && newEnd.isAfter(oldStart);

            if (!trungThoiGian) {
                continue;
            }

            for (ChiTietDatBanBan chiTiet : datBan.getChiTietDatBanBans()) {
                if (chiTiet.getBan() == null) {
                    continue;
                }

                if (dsBan.contains(chiTiet.getBan().getIdBan())) {
                    throw new IllegalArgumentException("Bàn " + chiTiet.getBan().getTenBan() + " đã được đặt trong khoảng thời gian này.");
                }
            }
        }
    }

    private void validateThoiGianHoatDong(LocalDateTime thoiGianDenDuKien) {

        LocalTime gio = thoiGianDenDuKien.toLocalTime();

        boolean caTrua =
                !gio.isBefore(LocalTime.of(10, 0))
                        && gio.isBefore(LocalTime.of(14, 0));

        boolean caToi =
                !gio.isBefore(LocalTime.of(18, 0))
                        && gio.isBefore(LocalTime.MAX);

        if (!caTrua && !caToi) {
            throw new RuntimeException(
                    "Nhà hàng nhận đơn đặt bàn chỉ từ 10:00-14:00 và 18:00-24:00."
            );
        }
    }

    private void validateSucChuaBan(List<Integer> dsBan, Integer soNguoi) {

        if (dsBan == null || dsBan.isEmpty()) {
            return;
        }

        int tongSucChua = dsBan.stream()
                .map(id -> banRepository.findById(id)
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn")))
                .mapToInt(b -> b.getLoaiBan().getSucChua())
                .sum();

        if (tongSucChua < soNguoi) {
            throw new IllegalArgumentException(
                    "Tổng sức chứa của các bàn được chọn không đủ phục vụ " + soNguoi + " khách."
            );
        }
    }

    //hàm đồng bộ trạng thái bàn
    private void capNhatTrangThaiBan(Integer idBan) {

        Ban ban = banRepository.findById(idBan)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

        // Bảo trì thì giữ nguyên
        if (ban.getTrangThai() == TrangThaiBan.BAO_TRI) {
            return;
        }

        List<DatBan> dsDatBan = datBanRepository.findByIdBanAndTrangThai(idBan,
                List.of(TrangThaiDatBan.DA_XAC_NHAN, TrangThaiDatBan.DA_NHAN_BAN));

        LocalDateTime now = LocalDateTime.now();

        TrangThaiBan trangThaiMoi = TrangThaiBan.TRONG;

        dsDatBan.sort((a, b) -> {

            if (a.getThoiGianDenDuKien() == null && b.getThoiGianDenDuKien() == null) {
                return 0;
            }

            if (a.getThoiGianDenDuKien() == null) {
                return 1;
            }

            if (b.getThoiGianDenDuKien() == null) {
                return -1;
            }

            return a.getThoiGianDenDuKien().compareTo(b.getThoiGianDenDuKien());
        });

        for (DatBan datBan: dsDatBan) {
            if (datBan.getThoiGianDenDuKien() == null) {
                continue;
            }

            LocalDateTime batDau = datBan.getThoiGianDenDuKien();
            LocalDateTime ketThuc = batDau.plusHours(THOI_GIAN_GIU_BAN);

            // Đơn đã hết hiệu lực
            if (now.isAfter(ketThuc)) {
                continue;
            }

            // Đang sử dụng ưu tiên cao nhất
            if (datBan.getTrangThai() == TrangThaiDatBan.DA_NHAN_BAN) {
                trangThaiMoi = TrangThaiBan.DANG_SU_DUNG;
                break;
            }

            // Đã xác nhận và tới giờ nhận bàn
            if (datBan.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN && !now.isBefore(batDau) && now.isBefore(ketThuc)) {
                trangThaiMoi = TrangThaiBan.DA_DAT;
            }
        }

        if (ban.getTrangThai() != trangThaiMoi) {
            ban.setTrangThai(trangThaiMoi);
            banRepository.save(ban);
        }
    }

    private void dongBoTatCaTrangThaiBan() {
        List<Ban> dsBan = banRepository.findAll();
        for (Ban ban : dsBan) {
            capNhatTrangThaiBan(ban.getIdBan());
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void autoSyncTrangThaiBan() {
        dongBoTatCaTrangThaiBan();
    }

    private TaiKhoan getCurrentTaiKhoan() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();

        return taiKhoanRepository.findByTenDangNhap(username)
                .orElse(null);
    }

    private void autoHuyDonQuaHan() {

        LocalDateTime now = LocalDateTime.now();

        List<DatBan> dsQuaHan = datBanRepository.findByTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);

        for (DatBan db : dsQuaHan) {

            if (db.getThoiGianDenDuKien() == null) {
                continue;
            }

            if (db.getThoiGianDenDuKien().plusMinutes(15).isBefore(now)) {

                db.setTrangThai(TrangThaiDatBan.DA_HUY);

                if (db.getChiTietDatBanBans() != null) {
                    for (ChiTietDatBanBan ct : db.getChiTietDatBanBans()) {
                        capNhatTrangThaiBan(ct.getBan().getIdBan());
                    }
                }
                // GỬI MAIL THÔNG BÁO HỦY ĐƠN QUÁ HẠN
                guiEmailThongBaoDatBan(
                        db,
                        "HUY_DON",
                        "Đơn đặt bàn bị tự động hủy do đã quá 15 phút so với thời gian dự kiến mà chưa được xác nhận/nhận bàn."
                );
            }
        }

        datBanRepository.saveAll(dsQuaHan);
    }
    //=============================================================================

    @Override
    public List<DTODatBanQuanLyResponse> getAll() {
        autoHuyDonQuaHan();
        return datBanRepository.findAll(Sort.by(Sort.Direction.DESC, "idDatBan"))
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
    //=================================================================
    @Override
    public DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d) {
        return add(d, getCurrentTaiKhoan());
    }

    @Override
    public DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d, TaiKhoan taiKhoan) {
        DatBan db = mapper.map(d, DatBan.class);
        db.setIdDatBan(null); // tránh model map láo map cả id đã tồn taại khi thêm

        // Khách hàng
        KhachHang khachHang;
        if (d.getIdKhachHang() != null) {
            khachHang = khachHangRepository.findById(d.getIdKhachHang())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));
        } else {
            // kiểm tra đã tồn tại theo SĐT chưa
            khachHang = khachHangRepository.findBySoDienThoai(d.getSdtKhachHang()).orElse(null);
            if (khachHang == null) {

                khachHang = new KhachHang();

                long count = khachHangRepository.count();
                String maTuSinh = String.format("KH%03d", count + 1);

                khachHang.setMaKhachHang(maTuSinh);
                khachHang.setTrangThai(true);

                khachHang.setTenKhachHang(d.getTenKhachHang());
                khachHang.setSoDienThoai(d.getSdtKhachHang());

                khachHang = khachHangRepository.save(khachHang);
            }
        }

        db.setKhachHang(khachHang);

        db.setSdtKhachHang(khachHang.getSoDienThoai());

        // Danh sách bàn
        if (d.getDsBan() != null && !d.getDsBan().isEmpty()) {

            List<ChiTietDatBanBan> dsBan = new ArrayList<>();

            for (Integer idBan : d.getDsBan()) {

                Ban ban = banRepository.findById(idBan)
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

                ChiTietDatBanBan ct = new ChiTietDatBanBan();
                ct.setDatBan(db);
                ct.setBan(ban);

                dsBan.add(ct);
            }

            db.setChiTietDatBanBans(dsBan);
        }

        // Danh sách combo
        if (d.getDsCombo() != null && !d.getDsCombo().isEmpty()) {

            List<ChiTietDatBanCombo> dsCombo = new ArrayList<>();

            for (var item : d.getDsCombo()) {

                Combo combo = comboRepository.findById(item.getIdCombo())
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy combo"));

                ChiTietDatBanCombo ct = new ChiTietDatBanCombo();
                ct.setDatBan(db);
                ct.setCombo(combo);
                ct.setSoLuong(item.getSoLuong());

                dsCombo.add(ct);
            }

            db.setChiTietDatBanCombos(dsCombo);
        }
        // Danh sách món
        if (d.getDsMon() != null && !d.getDsMon().isEmpty()) {

            List<ChiTietDatBanMon> dsMon = new ArrayList<>();

            for (DTOChiTietDatBanMonRequest item : d.getDsMon()) {

                Mon mon = monRepository.findById(item.getIdMon())
                        .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy món"));

                ChiTietDatBanMon ct = new ChiTietDatBanMon();
                ct.setDatBan(db);
                ct.setMon(mon);
                ct.setSoLuong(item.getSoLuong());

                dsMon.add(ct);
            }

            db.setChiTietDatBanMons(dsMon);
        }

        db.setNgayDat(LocalDate.now());
        db.setGioDat(Time.valueOf(LocalTime.now()));

        if (db.getSoTienCoc() == null || db.getSoTienCoc().compareTo(BigDecimal.ZERO) <= 0) {
            db.setSoTienCoc(BigDecimal.ZERO);
            db.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);
        } else {
            db.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);
        }

        db.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN);

        validateThoiGianHoatDong(d.getThoiGianDenDuKien());
        validateSucChuaBan(d.getDsBan(), d.getSoNguoi());
        validateDanhSachBan(d.getDsBan(), d.getThoiGianDenDuKien(), null);
        // Ghi nhận tài khoản tạo đơn
        db.setTaiKhoanTao(taiKhoan);


        db = datBanRepository.saveAndFlush(db);
//        datBanRepository.save(db);

        for (ChiTietDatBanBan ct: db.getChiTietDatBanBans()) {
            capNhatTrangThaiBan(ct.getBan().getIdBan());
        }
        return mapToResponse(db);
    }

    @Override
    public List<DTODatBanQuanLyResponse> search(String keyword, TrangThaiDatBan trangThai, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return datBanRepository.search(keyword, trangThai, tuNgay, denNgay)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DTODatBanQuanLyResponse hoanThanh(Integer id) {
        DatBan db = datBanRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
        db.setTrangThai(TrangThaiDatBan.HOAN_THANH);
        datBanRepository.save(db);
        for (ChiTietDatBanBan ct : db.getChiTietDatBanBans()) {
            capNhatTrangThaiBan(ct.getBan().getIdBan());
        }
        return mapToResponse(db);
    }

    @Override
    public List<DTOBanResponse> tinhTrangBan(LocalDateTime thoiGianDenDuKien) {

        if (thoiGianDenDuKien == null) {
            throw new IllegalArgumentException("Thời gian muốn kiểm tra không được để trống.");
        }
        LocalDateTime thoiGianKetThuc = thoiGianDenDuKien.plusHours(THOI_GIAN_GIU_BAN);

        // Lấy toàn bộ bàn
        List<Ban> dsBan = banRepository.findAll();

        // Các đơn có khả năng chiếm bàn
        List<DatBan> dsDatBan = datBanRepository.
                findByTrangThaiIn(List.of(TrangThaiDatBan.CHO_XAC_NHAN, TrangThaiDatBan.DA_XAC_NHAN, TrangThaiDatBan.DA_NHAN_BAN));

        /*
         * Lưu trạng thái tạm của từng bàn.
         * Không sửa trạng thái thật trong entity Ban.
         */
        java.util.Map<Integer, TrangThaiBan> trangThaiTam = new java.util.HashMap<>();

        // Khởi tạo trạng thái từ trạng thái thật hiện tại của bàn
        for (Ban ban : dsBan) {
            trangThaiTam.put(ban.getIdBan(), ban.getTrangThai());
        }

        // Kiểm tra các đơn giao với khoảng thời gian đang xem
        for (DatBan datBan : dsDatBan) {
            if (datBan.getThoiGianDenDuKien() == null) {
                continue;
            }

            LocalDateTime batDau = datBan.getThoiGianDenDuKien();
            LocalDateTime ketThuc = batDau.plusHours(THOI_GIAN_GIU_BAN);

            boolean trungThoiGian = thoiGianDenDuKien.isBefore(ketThuc) && thoiGianKetThuc.isAfter(batDau);

            if (!trungThoiGian) {
                continue;
            }

            if (datBan.getChiTietDatBanBans() == null) {
                continue;
            }

            for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
                if (ct.getBan() == null) {
                    continue;
                }
                Integer idBan = ct.getBan().getIdBan();

                // Bàn bảo trì luôn giữ nguyên
                if (ct.getBan().getTrangThai() == TrangThaiBan.BAO_TRI) {
                    continue;
                }

                /*
                 * Ưu tiên:
                 * DANG_SU_DUNG > DA_DAT > TRONG
                 */
                if (datBan.getTrangThai() == TrangThaiDatBan.DA_NHAN_BAN) {
                    trangThaiTam.put(idBan, TrangThaiBan.DANG_SU_DUNG);

                } else if (datBan.getTrangThai() == TrangThaiDatBan.DA_XAC_NHAN
                        || datBan.getTrangThai() == TrangThaiDatBan.CHO_XAC_NHAN) {
                    TrangThaiBan trangThaiHienTai = trangThaiTam.get(idBan);

                    if (trangThaiHienTai != TrangThaiBan.DANG_SU_DUNG) {
                        trangThaiTam.put(idBan, TrangThaiBan.DA_DAT);
                    }
                }
            }
        }

        // Convert sang DTO
        return dsBan.stream()
                .map(ban -> {
                    DTOBanResponse dto = mapper.map(ban, DTOBanResponse.class);
                    dto.setSucChua(ban.getLoaiBan().getSucChua());

                    // Gán trạng thái tạm để trả về FE
                    dto.setTrangThai(trangThaiTam.get(ban.getIdBan()));

                    return dto;
                })
                .toList();
    }

    //=============================================================
    @Override
    public DTODatBanQuanLyResponse update(Integer id, DTODatBanQuanLyRequest d) {
        return datBanRepository.findById(id)
                .map(db -> {
                    validateDanhSachBan(d.getDsBan(), d.getThoiGianDenDuKien(), id);
                    List<Integer> dsBanCu = db.getChiTietDatBanBans()
                            .stream()
                            .map(ct -> ct.getBan().getIdBan())
                            .toList();

                    if (db.getChiTietDatBanBans() == null) {
                        db.setChiTietDatBanBans(new ArrayList<>());
                    } else {
                        db.getChiTietDatBanBans().clear();
                    }


                    // Danh sách bàn
                    if (d.getDsBan() != null) {
                        if (db.getChiTietDatBanBans() == null) {
                            db.setChiTietDatBanBans(new ArrayList<>());
                        } else {
                            db.getChiTietDatBanBans().clear();
                        }

                        for (Integer idBan : d.getDsBan()) {
                            Ban ban = banRepository.findById(idBan)
                                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

                            ChiTietDatBanBan chiTiet = new ChiTietDatBanBan();
                            chiTiet.setDatBan(db);
                            chiTiet.setBan(ban);

                            db.getChiTietDatBanBans().add(chiTiet);
                        }
                    }

                    if (d.getDsCombo() != null) {
                        if (db.getChiTietDatBanCombos() == null) {
                            db.setChiTietDatBanCombos(new ArrayList<>());
                        } else {
                            db.getChiTietDatBanCombos().clear();
                        }

                        List<ChiTietDatBanCombo> chiTietDatBanCombos = new ArrayList<>();

                        d.getDsCombo().forEach(item -> {
                            Combo combo = comboRepository.findById(item.getIdCombo())
                                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy combo"));

                            ChiTietDatBanCombo chiTiet = new ChiTietDatBanCombo();
                            chiTiet.setDatBan(db);
                            chiTiet.setCombo(combo);
                            chiTiet.setSoLuong(item.getSoLuong());

                            chiTietDatBanCombos.add(chiTiet);
                        });

                        db.setChiTietDatBanCombos(chiTietDatBanCombos);
                    }
                    if (d.getDsMon() != null) {

                        if (db.getChiTietDatBanMons() == null) {
                            db.setChiTietDatBanMons(new ArrayList<>());
                        } else {
                            db.getChiTietDatBanMons().clear();
                        }

                        List<ChiTietDatBanMon> chiTietDatBanMons = new ArrayList<>();

                        d.getDsMon().forEach(item -> {

                            Mon mon = monRepository.findById(item.getIdMon())
                                    .orElseThrow(() ->
                                            new CustomResourceNotFoundException("Không tìm thấy món"));

                            ChiTietDatBanMon chiTiet = new ChiTietDatBanMon();
                            chiTiet.setDatBan(db);
                            chiTiet.setMon(mon);
                            chiTiet.setSoLuong(item.getSoLuong());

                            chiTietDatBanMons.add(chiTiet);
                        });

                        db.setChiTietDatBanMons(chiTietDatBanMons);
                    }

                    if (d.getIdKhachHang() != null) {
                        KhachHang kh = khachHangRepository.findById(d.getIdKhachHang())
                                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));
                        db.setKhachHang(kh);
                    } else {
                        db.setKhachHang(null);
                    }

                    if (d.getSdtKhachHang() != null && !d.getSdtKhachHang().isBlank()) db.setSdtKhachHang(d.getSdtKhachHang());

                    if (d.getSoNguoi() > 0) db.setSoNguoi(d.getSoNguoi());

                    if (d.getTrangThai() != null) db.setTrangThai(d.getTrangThai());

                    if (d.getGhiChu() != null) db.setGhiChu(d.getGhiChu());

                    if (d.getThoiGianDenDuKien() != null) db.setThoiGianDenDuKien(d.getThoiGianDenDuKien());

                    if (d.getSoTienCoc() != null) db.setSoTienCoc(d.getSoTienCoc());
                    else if (db.getSoTienCoc() == null) db.setSoTienCoc(BigDecimal.ZERO);

                    if (d.getTrangThaiCoc() != null) db.setTrangThaiCoc(d.getTrangThaiCoc());

                    if (d.getPhuongThucThanhToan() != null) db.setPhuongThucThanhToan(d.getPhuongThucThanhToan());

                    validateThoiGianHoatDong(d.getThoiGianDenDuKien());
                    validateSucChuaBan(d.getDsBan(), d.getSoNguoi());
                    validateDanhSachBan(d.getDsBan(), d.getThoiGianDenDuKien(), null);
                    datBanRepository.save(db);

                    for (Integer idBan : dsBanCu) {
                        capNhatTrangThaiBan(idBan);
                    }

                    for (ChiTietDatBanBan ct : db.getChiTietDatBanBans()) {
                        capNhatTrangThaiBan(ct.getBan().getIdBan());
                    }

                    return mapToResponse(db);
                }).orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
    }

    @Override
    public void delete(Integer id) {
        DatBan datBan = datBanRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));

        datBan.setTrangThai(TrangThaiDatBan.DA_HUY);
        // Ghi nhận tài khoản hủy đơn
        datBan.setTaiKhoanHuy(getCurrentTaiKhoan());
        datBanRepository.save(datBan);

        if (datBan.getChiTietDatBanBans() != null) {
            for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
                if (ct.getBan() != null) {
                    capNhatTrangThaiBan(ct.getBan().getIdBan());
                }
            }
        }
        // GỬI MAIL THÔNG BÁO HỦY BÀN CHO KHÁCH HÀNG
        guiEmailThongBaoDatBan(datBan, "HUY_DON", "Đơn hàng đã bị hủy bởi nhân viên quản lý nhà hàng.");
    }

    @Override
    public List<DTOBanResponse> getDanhSachBanTrong(LocalDateTime thoiGianDenDuKien,Integer soNguoi, Integer idDatBan) {

        LocalDateTime newStart = thoiGianDenDuKien;
        LocalDateTime newEnd = newStart.plusHours(THOI_GIAN_GIU_BAN);

        List<Ban> tatCaBan = banRepository.findAll();

        List<DatBan> dsDatBan = datBanRepository.findByTrangThaiIn(
                List.of(
                        TrangThaiDatBan.CHO_XAC_NHAN,
                        TrangThaiDatBan.DA_XAC_NHAN,
                        TrangThaiDatBan.DA_NHAN_BAN
                )
        );

        // Nếu đang sửa thì bỏ qua chính đơn đó
        if (idDatBan != null) {
            dsDatBan.removeIf(db -> db.getIdDatBan().equals(idDatBan));
        }

        Set<Integer> dsBanDaDat = new HashSet<>();

        for (DatBan datBan : dsDatBan) {

            if (datBan.getThoiGianDenDuKien() == null) {
                continue;
            }

            LocalDateTime oldStart = datBan.getThoiGianDenDuKien();
            LocalDateTime oldEnd = oldStart.plusHours(THOI_GIAN_GIU_BAN);

            boolean trungThoiGian =
                    newStart.isBefore(oldEnd)
                            && newEnd.isAfter(oldStart);

            if (!trungThoiGian) {
                continue;
            }

            if (datBan.getChiTietDatBanBans() == null) {
                continue;
            }

            for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {

                if (ct.getBan() != null) {
                    dsBanDaDat.add(ct.getBan().getIdBan());
                }
            }
        }

        List<Ban> dsBanTrong = tatCaBan.stream()
                .filter(ban -> ban.getTrangThai() != TrangThaiBan.BAO_TRI)
                .filter(ban -> !dsBanDaDat.contains(ban.getIdBan()))
                .toList();

        int tongSucChua = dsBanTrong.stream()
                .mapToInt(b -> b.getLoaiBan().getSucChua())
                .sum();

        if (tongSucChua < soNguoi) {
            throw new RuntimeException("Không đủ bàn phục vụ.");
        }
        return dsBanTrong.stream()
                .map(ban -> {
                    DTOBanResponse dto = mapper.map(ban, DTOBanResponse.class);

                    dto.setSucChua(ban.getLoaiBan().getSucChua());

                    return dto;
                })
                .toList();
    }

    @Override
    public DTODatBanQuanLyResponse xacNhan(Integer id) {

        DatBan datBan = datBanRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));

        if (datBan.getTrangThai() != TrangThaiDatBan.CHO_XAC_NHAN) {
            throw new IllegalArgumentException("Chỉ được xác nhận đơn đang chờ xác nhận.");
        }

        datBan.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN);

        // Ghi nhận tài khoản xác nhận
        datBan.setTaiKhoanXacNhan(getCurrentTaiKhoan());

        datBanRepository.save(datBan);

        if (datBan.getChiTietDatBanBans() != null) {
            for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
                capNhatTrangThaiBan(ct.getBan().getIdBan());
            }
        }

        // --- GỬI MAIL THÔNG BÁO CHO KHÁCH HÀNG ---
        guiEmailThongBaoDatBan(datBan, "XAC_NHAN", null);

        return mapToResponse(datBan);
    }

    // Hàm phụ trách tạo template HTML và thực thi gửi email
    @Async
    public void guiEmailThongBaoDatBan(DatBan datBan, String loaiHanhDong, String lyDoHuy) {
        try {
            KhachHang khachHang = datBan.getKhachHang();
            if (khachHang == null || khachHang.getEmail() == null || khachHang.getEmail().isBlank()) {
                System.out.println("Bỏ qua gửi email: Không tìm thấy Email khách hàng.");
                return;
            }

            String emailKhach = khachHang.getEmail();
            String tenKhach = khachHang.getTenKhachHang();
            String thoiGian = datBan.getThoiGianDenDuKien() != null
                    ? datBan.getThoiGianDenDuKien().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))
                    : "Đang cập nhật";

            // Gom thông tin bàn + khu vực
            List<String> thongTinBanList = new ArrayList<>();
            if (datBan.getChiTietDatBanBans() != null && !datBan.getChiTietDatBanBans().isEmpty()) {
                for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
                    if (ct.getBan() != null) {
                        Ban ban = ct.getBan();
                        String tenBan = ban.getTenBan();
                        String tenKhuVuc = (ban.getKhuVuc() != null && ban.getKhuVuc().getTenKhuVuc() != null)
                                ? ban.getKhuVuc().getTenKhuVuc()
                                : "Sảnh chính";
                        thongTinBanList.add(tenBan + " (" + tenKhuVuc + ")");
                    }
                }
            }

            String chuoiBanVakhuVuc = thongTinBanList.isEmpty()
                    ? "Đã sắp xếp tại nhà hàng"
                    : String.join(", ", thongTinBanList);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailKhach);

            String subject = "";
            String titleHtml = "";
            String subTitleHtml = "";
            String mainColor = "#c5a059";
            String detailBoxBg = "#f9f9f9";

            switch (loaiHanhDong) {
                case "XAC_NHAN":
                    subject = "HOTPOT RESTAURANT - Xác Nhận Đặt Bàn Thành Công!";
                    titleHtml = "<h3 style='color: #2e7d32; text-align: center;'>Bàn của bạn đã được xác nhận!</h3>";
                    subTitleHtml = "<p>Nhà hàng đã tiếp nhận và xác nhận đơn đặt bàn của bạn thành công. Dưới đây là thông tin chi tiết:</p>";
                    break;

                case "DOI_GIO":
                    subject = "HOTPOT RESTAURANT - Cập Nhật Thời Gian Đặt Bàn";
                    titleHtml = "<h3 style='color: #0288d1; text-align: center;'>Thời gian đặt bàn đã được cập nhật!</h3>";
                    subTitleHtml = "<p>Nhà hàng đã cập nhật <b>thời gian đến dự kiến mới</b> cho đơn đặt bàn của bạn:</p>";
                    mainColor = "#0288d1";
                    detailBoxBg = "#f0f8ff";
                    break;

                case "DOI_BAN":
                    subject = "HOTPOT RESTAURANT - Cập Nhật Vị Trí Bàn Mới";
                    titleHtml = "<h3 style='color: #ed6c02; text-align: center;'>Vị trí bàn đặt đã được thay đổi!</h3>";
                    subTitleHtml = "<p>Nhà hàng đã thay đổi <b>vị trí bàn / khu vực mới</b> cho đơn đặt bàn của bạn:</p>";
                    mainColor = "#ed6c02";
                    detailBoxBg = "#fff8f0";
                    break;

                case "HUY_DON":
                    subject = "HOTPOT RESTAURANT - Thông Báo Hủy Đặt Bàn";
                    titleHtml = "<h3 style='color: #d32f2f; text-align: center;'>Thông Báo Hủy Đơn Đặt Bàn</h3>";
                    subTitleHtml = "<p>Rất tiếc, nhà hàng xin thông báo đơn đặt bàn của bạn đã bị <b>HỦY</b>.</p>";
                    mainColor = "#d32f2f";
                    detailBoxBg = "#fff5f5";
                    break;

                default:
                    return;
            }

            helper.setSubject(subject);

            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid ").append(mainColor).append("; border-radius: 8px; max-width: 600px; margin: 0 auto;'>")
                    .append("<h2 style='color: #c5a059; text-align: center;'>HOTPOT RESTAURANT</h2>")
                    .append(titleHtml)
                    .append("<p>Xin chào <b>").append(tenKhach).append("</b>,</p>")
                    .append(subTitleHtml)
                    .append("<ul style='background: ").append(detailBoxBg).append("; padding: 15px 30px; border-radius: 5px; list-style-type: square; line-height: 1.8;'>")
                    .append("  <li><b>Mã đơn đặt bàn:</b> #").append(datBan.getIdDatBan()).append("</li>")
                    .append("  <li><b>Thời gian đến dự kiến:</b> <span style='font-weight: bold;'>").append(thoiGian).append("</span></li>")
                    .append("  <li><b>Số lượng khách:</b> ").append(datBan.getSoNguoi()).append(" người</li>");

            if ("HUY_DON".equals(loaiHanhDong)) {
                String lyDoText = (lyDoHuy != null && !lyDoHuy.isBlank()) ? lyDoHuy : "Do hết bàn hoặc nhà hàng có sự cố đột xuất.";
                html.append("  <li><b>Lý do hủy:</b> <span style='color: #d32f2f; font-weight: bold;'>").append(lyDoText).append("</span></li>");
            } else {
                html.append("  <li><b>Vị trí bàn & Khu vực:</b> <span style='color: #d32f2f; font-weight: bold;'>").append(chuoiBanVakhuVuc).append("</span></li>");
            }

            html.append("</ul>");

            if ("HUY_DON".equals(loaiHanhDong)) {
                html.append("<p>Chúng tôi thành thật xin lỗi vì sự bất tiện này. Rất mong quý khách thông cảm và hân hạnh được phục vụ vào lần sau.</p>");
            } else {
                html.append("<p>Vui lòng đến đúng giờ để nhà hàng phục vụ chu đáo nhất. Mọi thắc mắc xin liên hệ hotline của nhà hàng.</p>");
            }

            html.append("<br><p>Trân trọng,<br><b>Đội ngũ Hotpot Restaurant</b></p></div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);

            System.out.println("Đã gửi email [" + loaiHanhDong + "] tới: " + emailKhach);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email thông báo: " + e.getMessage());
        }
    }

    @Override
    public DTODatBanQuanLyResponse checkIn(Integer id) {

        DatBan datBan = datBanRepository.findById(id)
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));

        if (datBan.getTrangThai() != TrangThaiDatBan.DA_XAC_NHAN) {
            throw new IllegalArgumentException(
                    "Chỉ được nhận bàn khi đơn đã xác nhận.");
        }

        datBan.setTrangThai(TrangThaiDatBan.DA_NHAN_BAN);

        datBanRepository.save(datBan);

        if (datBan.getChiTietDatBanBans() != null) {
            for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
                capNhatTrangThaiBan(ct.getBan().getIdBan());
            }
        }

        return mapToResponse(datBan);
    }

    @Override
    public DTODatBanQuanLyResponse doiGio(Integer id, DTODoiGioRequest request) {

        DatBan datBan = datBanRepository.findById(id)
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));

        List<Integer> dsBan = datBan.getChiTietDatBanBans()
                .stream()
                .map(ct -> ct.getBan().getIdBan())
                .toList();

        validateDanhSachBan(dsBan, request.getThoiGianMoi(), id);

        datBan.setThoiGianDenDuKien(request.getThoiGianMoi());

        datBanRepository.save(datBan);

        for (ChiTietDatBanBan ct : datBan.getChiTietDatBanBans()) {
            capNhatTrangThaiBan(ct.getBan().getIdBan());
        }

        // --- GỬI MAIL THÔNG BÁO CHO KHÁCH HÀNG ---
        guiEmailThongBaoDatBan(datBan, "DOI_GIO", null);
        return mapToResponse(datBan);
    }

    @Override
    @Transactional
    public DTODatBanQuanLyResponse doiBan(Integer id, DTODoiBanRequest request) {

        DatBan datBan = datBanRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));

        List<Integer> dsBanCu = datBan.getChiTietDatBanBans() == null
                ? new ArrayList<>()
                : datBan.getChiTietDatBanBans()
                .stream()
                .map(ct -> ct.getBan().getIdBan())
                .distinct()
                .toList();

        // Không được để trống
        if (request.getDsBan() == null || request.getDsBan().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn bàn mới.");
        }

        // Loại bỏ id trùng
        List<Integer> dsBanMoi = request.getDsBan()
                .stream()
                .distinct()
                .toList();

        // Không cho đổi sang đúng bàn cũ
        if (new HashSet<>(dsBanMoi).equals(new HashSet<>(dsBanCu))) {
            throw new IllegalArgumentException("Vui lòng chọn bàn khác bàn hiện tại.");
        }

        // Validate nghiệp vụ
        validateDanhSachBan(dsBanMoi, datBan.getThoiGianDenDuKien(), id);

        // Đảm bảo collection không null
        if (datBan.getChiTietDatBanBans() == null) {
            datBan.setChiTietDatBanBans(new ArrayList<>());
        }

        // Xóa toàn bộ liên kết cũ
        datBan.getChiTietDatBanBans().clear();

        // Flush để Hibernate xóa orphan trước khi insert mới
        datBanRepository.saveAndFlush(datBan);

        // Thêm bàn mới
        for (Integer idBan : dsBanMoi) {

            Ban ban = banRepository.findById(idBan)
                    .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));

            ChiTietDatBanBan chiTiet = new ChiTietDatBanBan();
            chiTiet.setDatBan(datBan);
            chiTiet.setBan(ban);

            datBan.getChiTietDatBanBans().add(chiTiet);
        }

        datBanRepository.saveAndFlush(datBan);

        // Đồng bộ trạng thái bàn cũ
        for (Integer idBan : dsBanCu) {
            capNhatTrangThaiBan(idBan);
        }

        // Đồng bộ trạng thái bàn mới
        for (Integer idBan : dsBanMoi) {
            capNhatTrangThaiBan(idBan);
        }

        // --- GỬI MAIL THÔNG BÁO CHO KHÁCH HÀNG ---
        guiEmailThongBaoDatBan(datBan, "DOI_BAN", null);
        return mapToResponse(datBan);
    }
    //===========================================================================
    @Scheduled(fixedDelay = 60000)
    public void autoCancelOverdueReservations() {

        LocalDateTime now = LocalDateTime.now();

        List<DatBan> pendingReservations = datBanRepository.findByTrangThaiIn(
                List.of(
                        TrangThaiDatBan.CHO_XAC_NHAN,
                        TrangThaiDatBan.DA_XAC_NHAN
                )
        );

        List<DatBan> overdueReservations = pendingReservations.stream()
                .filter(datBan -> shouldAutoCancel(datBan, now))
                .toList();

        for (DatBan overdueReservation : overdueReservations) {
            overdueReservation.setTrangThai(TrangThaiDatBan.DA_HUY);
            datBanRepository.save(overdueReservation);

            if (overdueReservation.getChiTietDatBanBans() != null) {
                for (ChiTietDatBanBan ct : overdueReservation.getChiTietDatBanBans()) {
                    if (ct.getBan() != null) {
                        capNhatTrangThaiBan(ct.getBan().getIdBan());
                    }
                }
            }
        }
    }

    private boolean shouldAutoCancel(DatBan datBan, LocalDateTime now) {
        LocalDateTime referenceTime = datBan.getThoiGianDenDuKien();
        if (referenceTime == null && datBan.getNgayDat() != null && datBan.getGioDat() != null) {
            referenceTime = LocalDateTime.of(datBan.getNgayDat(), datBan.getGioDat().toLocalTime());
        }

        if (referenceTime == null) {
            return false;
        }

        return !now.isBefore(referenceTime.plusMinutes(15));
    }

    private boolean isVisibleReservation(DatBan datBan) {
        return !hoaDonRepository.existsByDatBan_IdDatBanAndTrangThaiThanhToan(datBan.getIdDatBan(), 1);
    }
}