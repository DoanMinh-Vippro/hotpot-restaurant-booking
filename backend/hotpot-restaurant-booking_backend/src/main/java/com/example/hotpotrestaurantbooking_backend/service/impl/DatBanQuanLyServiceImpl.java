package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DatBanQuanLyServiceImpl implements DatBanQuanLyService {
    private final ModelMapper mapper;
    private final DatBanRepository datBanRepository;
    private final BanRepository banRepository;
    private final KhachHangRepository khachHangRepository;
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
                    response.setIdBan(d.getBan().getIdBan());
                    response.setIdKhachHang(d.getKhachHang().getIdKhachHang());
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
                    response.setIdBan(d.getBan().getIdBan());
                    response.setIdKhachHang(d.getKhachHang().getIdKhachHang());
                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay don dat ban"));
    }

    @Override
    public DTODatBanQuanLyResponse add(DTODatBanQuanLyRequest d) {
        // 1. Map cơ bản các trường dữ liệu thường
        DatBan db = mapper.map(d, DatBan.class);

        // 2. TÌM KIẾM ĐỐI TƯỢNG LIÊN KẾT (CẦN THIẾT)
        Ban ban = banRepository.findById(d.getIdBan())
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));
        KhachHang kh = khachHangRepository.findById(d.getIdkhachHang())
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));

        // 3. GÁN ĐỐI TƯỢNG ĐÃ ĐƯỢC QUẢN LÝ BỞI HIBERNATE
        db.setBan(ban);
        db.setKhachHang(kh);

        db.setGioDat(LocalTime.now());
        db.setNgayDat(LocalDate.now());

        // 4. Save
        datBanRepository.save(db);

        // 5. Trả về response
        DTODatBanQuanLyResponse response = mapper.map(db, DTODatBanQuanLyResponse.class);
        response.setLoaiBan(ban.getLoaiBan());
        response.setTenKhachHang(kh.getTenKhachHang());
        response.setIdBan(db.getBan().getIdBan());
        response.setIdKhachHang(db.getKhachHang().getIdKhachHang());
        return response;
    }

    @Override
    public DTODatBanQuanLyResponse update(Integer id, DTODatBanQuanLyRequest d) {
        return datBanRepository
                .findById(id)
                .map(db -> {
                    // 1. Cập nhật đối tượng Bàn (Phải tìm từ Repository)
                    if (d.getIdBan() != null) {
                        Ban ban = banRepository.findById(d.getIdBan())
                                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy bàn"));
                        db.setBan(ban);
                    }

                    // 2. Cập nhật đối tượng Khách hàng
                    if (d.getIdkhachHang() != null) {
                        KhachHang kh = khachHangRepository.findById(d.getIdkhachHang())
                                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khách hàng"));
                        db.setKhachHang(kh);
                    }

                    // 3. Cập nhật các trường thông tin đơn giản
                    if (d.getSdtKhachHang() != null && !d.getSdtKhachHang().isBlank()) db.setSdtKhachHang(d.getSdtKhachHang());
                    if (d.getSoNguoi() > 0) db.setSoNguoi(d.getSoNguoi());
                    if (d.getTrangThai() != null) db.setTrangThai(d.getTrangThai());
                    if (d.getGhiChu() != null) db.setGhiChu(d.getGhiChu());
                    if (d.getThoiGianDenDuKien() != null) db.setThoiGianDenDuKien(d.getThoiGianDenDuKien());
                    if (d.getSoTienCoc() != null) db.setSoTienCoc(d.getSoTienCoc());
                    if (d.getTrangThaiCoc() != null) db.setTrangThaiCoc(d.getTrangThaiCoc());
                    if (d.getPhuongThucThanhToan() != null) db.setPhuongThucThanhToan(d.getPhuongThucThanhToan());

                    // 4. LƯU THAY ĐỔI VÀO DATABASE
                    datBanRepository.save(db);

                    // 5. Trả về response
                    DTODatBanQuanLyResponse response = mapper.map(db, DTODatBanQuanLyResponse.class);
                    response.setLoaiBan(db.getBan().getLoaiBan());
                    response.setTenKhachHang(db.getKhachHang().getTenKhachHang());
                    response.setIdKhachHang(db.getKhachHang().getIdKhachHang());
                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy đơn đặt bàn"));
    }

    @Override
    public void delete(Integer id) {
        datBanRepository.deleteById(id);
    }
}
