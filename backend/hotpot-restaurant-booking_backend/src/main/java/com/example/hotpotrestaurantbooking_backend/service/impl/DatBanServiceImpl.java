package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Combo;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.ComboRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private static final String COMBO_NULL_MSG = "Đơn đặt bàn này không chọn combo đặt trước";
    private final KhachHangRepository khachHangRepository;

    private void setComboInfo(DatBan db, DTODatBanResponse res) {
        // Kiểm tra null của cả đối tượng Combo trước
        if (db.getCombo() != null && db.getCombo().getIdCombo() != null) {
            res.setIdCombo(db.getCombo().getIdCombo());
            res.setTenCombo(db.getCombo().getTenCombo());
        } else {
            res.setTenCombo(COMBO_NULL_MSG);
            res.setIdCombo(null); // Đảm bảo trả về null cho FE dễ nhận diện
        }
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
        d.setBan(null); // tránh ModelMapper map nhầm Ban cũ gây lỗi FK id_ban khi insert
        d.setKhachHang(khachHang);

//        // PHẢI TÌM VÀ SET COMBO THỦ CÔNG
//        if (datBan.getIdCombo() != null) {
//            // CÁCH TỐI ƯU: Tạo một đối tượng Combo "rỗng" chỉ chứa ID
//            // Cách này giúp Hibernate không bao giờ "nhìn" thấy các bản ghi cũ
//            // Dùng Proxy Object thay vì comboRepository.findById()
//            // Lý do: Khi dùng findById(), Hibernate sẽ quản lý đối tượng Combo trong Persistence Context,
//            // điều này vô tình gây ra lỗi "dirty checking" hoặc cập nhật nhầm các bản ghi cũ liên quan.
//            // Việc chỉ set ID (giả lập Proxy) giúp Hibernate coi đây là Foreign Key đơn thuần,
//            // đảm bảo lệnh save() luôn thực hiện INSERT thay vì UPDATE nhầm.
//            Combo proxyCombo = new Combo();
//            proxyCombo.setIdCombo(datBan.getIdCombo());
//            d.setCombo(proxyCombo);
//        } else {
//            d.setCombo(null);
//        }

        if (datBan.getIdCombo() != null) {
            Combo combo = comboRepository.findById(datBan.getIdCombo())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Combo không tồn tại"));

            d.setCombo(combo);
        } else {
            d.setCombo(null);
        }

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());
        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.CHUA_COC);
        if (d.getSoTienCoc() == null) {
            d.setSoTienCoc(BigDecimal.ZERO);
        }
        if (d.getCombo() == null && d.getSoTienCoc() != null) {
            d.setSoTienCoc(BigDecimal.ZERO);
        }
        datBanRepository.save(d);

        DTODatBanResponse res = mapper.map(d, DTODatBanResponse.class);
        setComboInfo(d, res);
        return res;
    }

    @Override
    public DTODatBanResponse update(Integer id, DTODatBanRequest datBan) {
        return datBanRepository.findById(id).map(db -> {
            if(datBan.getSdtKhachHang() != null && !datBan.getSdtKhachHang().isBlank()) db.setSdtKhachHang(datBan.getSdtKhachHang());
            if(datBan.getSoNguoi() != null) db.setSoNguoi(datBan.getSoNguoi());
            if(datBan.getThoiGianDenDuKien() != null) db.setThoiGianDenDuKien(datBan.getThoiGianDenDuKien());
            if(datBan.getSoTienCoc() != null) db.setSoTienCoc(datBan.getSoTienCoc());
            if(datBan.getPhuongThucThanhToan() != null) db.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan());
            if(datBan.getGhiChu() != null) db.setGhiChu(datBan.getGhiChu());
// PHẢI TÌM VÀ SET COMBO THỦ CÔNG
            if (datBan.getIdCombo() != null) {
                db.setCombo(comboRepository.findById(datBan.getIdCombo())
                        .orElseThrow(() -> new CustomResourceNotFoundException("Combo này không tồn tại trong hệ thống!")));
            } else {
                db.setCombo(null);
                db.setSoTienCoc(BigDecimal.ZERO);
            }

            if (db.getCombo() == null && db.getSoTienCoc() == null) {
                db.setSoTienCoc(BigDecimal.ZERO);
            }

            datBanRepository.save(db);

            DTODatBanResponse res = mapper.map(db, DTODatBanResponse.class);
            setComboInfo(db, res);
            return res;
        }).orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay don dat ban"));
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


    public DatBan createBookingAfterPayment(Integer idKhachHang, DTODatBanRequest datBan) {
        DatBan d = mapper.map(datBan, DatBan.class);

        KhachHang khachHang = khachHangRepository
                .findById(idKhachHang)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));

        d.setIdDatBan(null);
        d.setBan(null);
        d.setKhachHang(khachHang);

        if (datBan.getIdCombo() != null) {
            Combo combo = comboRepository.findById(datBan.getIdCombo())
                    .orElseThrow(() -> new CustomResourceNotFoundException("Combo không tồn tại"));

            d.setCombo(combo);
        } else {
            d.setCombo(null);
        }

        d.setGioDat(Time.valueOf(LocalTime.now()));
        d.setNgayDat(LocalDate.now());

        d.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
        d.setTrangThaiCoc(TrangThaiDatBanCoc.DA_COC);

        datBanRepository.save(d);

        return d;
    }


}
