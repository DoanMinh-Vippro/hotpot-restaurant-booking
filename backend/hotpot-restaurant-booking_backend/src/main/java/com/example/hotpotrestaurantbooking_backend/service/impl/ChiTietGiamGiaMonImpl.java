package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.Validation.ChiTietGiamGiaMonValidator;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonRequest;
import com.example.hotpotrestaurantbooking_backend.dto.ChiTietGiamGiaMonResponse;
import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.entity.DotGiamGia;
import com.example.hotpotrestaurantbooking_backend.entity.Mon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DotGiamGiaRepository;
import com.example.hotpotrestaurantbooking_backend.repository.MonRepository;
import com.example.hotpotrestaurantbooking_backend.service.ChiTietGiamGiaMonService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ChiTietGiamGiaMonImpl implements ChiTietGiamGiaMonService {
    @Autowired
    private ChiTietGiamGiaMonRepository repo;
    @Autowired
    private ChiTietGiamGiaMonValidator validator;
    @Autowired
    private DotGiamGiaRepository repo2;
    @Autowired
    private MonRepository repo3;

    public List<ChiTietGiamGiaMonResponse> hienThiCTGGM(){
        return repo.hienThiCTGGM();
    }

    public ChiTietGiamGiaMonResponse detailCTGGM(Integer idChiTietGiamGiaMon){
        return repo.detailCTGGM(idChiTietGiamGiaMon);
    }

    public Page<ChiTietGiamGiaMonResponse> phanTrangCTGGM(Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return repo.phanTrangCTGGM(pageable);
    }

    public Page<ChiTietGiamGiaMonResponse> timKiemCTGGM(String tenChuongTrinh, String tenMon,BigDecimal mucMin, BigDecimal mucMax, String loaiGiam,
                                                        Integer pageNo, Integer pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        String tenChuongTrinhSearch = (tenChuongTrinh != null && !tenChuongTrinh.trim().isEmpty())
                ? "%" + tenChuongTrinh.trim() + "%"
                : null;

        String tenMonSearch = (tenMon != null && !tenMon.trim().isEmpty())
                ? "%" + tenMon.trim() + "%"
                : null;
        String loaiGiamSearch = (loaiGiam != null) ? loaiGiam.trim() : null;
        return repo.timKiemCTGGM(tenChuongTrinhSearch, tenMonSearch, mucMin, mucMax, loaiGiamSearch, pageable);
    }

    public void addCTGGM(ChiTietGiamGiaMonRequest req){
        validator.validateAdd(req);

        DotGiamGia dgg = repo2.findById(req.getIdDotGiamGia())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đợt giảm giá"));

        List<Integer> listIdMon = req.getDanhSachMonId();

        // 1. Trường hợp gửi lên mảng danh sách nhiều món (Checkbox từ FE)
        if (listIdMon != null && !listIdMon.isEmpty()) {
            for (Integer idMon : listIdMon) {
                Mon m = repo3.findById(idMon)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy Món có ID: " + idMon));

                ChiTietGiamGiaMon ctggm = new ChiTietGiamGiaMon();
                ctggm.setDotGiamGia(dgg);
                ctggm.setMon(m);
                ctggm.setMucGiam(req.getMucGiam());
                ctggm.setLoaiGiam(req.getLoaiGiam());
                ctggm.setTrangThai(req.getTrangThai());

                repo.save(ctggm);
            }
        }
        // 2. Fallback trường hợp gửi 1 món lẻ
        else if (req.getIdMon() != null) {
            Mon m = repo3.findById(req.getIdMon())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Món"));

            ChiTietGiamGiaMon ctggm = new ChiTietGiamGiaMon();
            ctggm.setDotGiamGia(dgg);
            ctggm.setMon(m);
            ctggm.setMucGiam(req.getMucGiam());
            ctggm.setLoaiGiam(req.getLoaiGiam());
            ctggm.setTrangThai(req.getTrangThai());

            repo.save(ctggm);
        }
    }

    public void updateCTGGM(Integer idChiTietGiamGiaMon,
                            ChiTietGiamGiaMonRequest req){
        validator.validateUpdate(idChiTietGiamGiaMon, req);

        ChiTietGiamGiaMon ctggm = repo.findById(idChiTietGiamGiaMon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chi tiết giảm giá món"));

        DotGiamGia dgg = repo2.findById(req.getIdDotGiamGia())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đợt giảm giá"));

        List<Integer> listIdMon = req.getDanhSachMonId();

        if (listIdMon != null && !listIdMon.isEmpty()) {
            // 1. Cập nhật bản ghi hiện tại với món đầu tiên trong danh sách
            Integer idMonDauTien = listIdMon.get(0);
            Mon mDauTien = repo3.findById(idMonDauTien)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Món có ID: " + idMonDauTien));

            ctggm.setDotGiamGia(dgg);
            ctggm.setMon(mDauTien);
            ctggm.setMucGiam(req.getMucGiam());
            ctggm.setLoaiGiam(req.getLoaiGiam());
            ctggm.setTrangThai(req.getTrangThai());
            repo.save(ctggm);

            // 2. Nếu chọn thêm các món khác chưa có trong đợt giảm giá -> Thêm mới bản ghi ChiTietGiamGiaMon cho các món đó
            for (int i = 1; i < listIdMon.size(); i++) {
                Integer idMonTiepTheo = listIdMon.get(i);

                // Kiểm tra xem món này đã tồn tại trong đợt giảm giá chưa
                if (!repo.existsByDotGiamGia_IdDotGiamGiaAndMon_IdMon(req.getIdDotGiamGia(), idMonTiepTheo)) {
                    Mon mTiepTheo = repo3.findById(idMonTiepTheo)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Món có ID: " + idMonTiepTheo));

                    ChiTietGiamGiaMon ctggmMoi = new ChiTietGiamGiaMon();
                    ctggmMoi.setDotGiamGia(dgg);
                    ctggmMoi.setMon(mTiepTheo);
                    ctggmMoi.setMucGiam(req.getMucGiam());
                    ctggmMoi.setLoaiGiam(req.getLoaiGiam());
                    ctggmMoi.setTrangThai(req.getTrangThai());

                    repo.save(ctggmMoi);
                }
            }
        } else if (req.getIdMon() != null) {
            Mon m = repo3.findById(req.getIdMon())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Món"));

            ctggm.setDotGiamGia(dgg);
            ctggm.setMon(m);
            ctggm.setMucGiam(req.getMucGiam());
            ctggm.setLoaiGiam(req.getLoaiGiam());
            ctggm.setTrangThai(req.getTrangThai());
            repo.save(ctggm);
        }
    }

}
