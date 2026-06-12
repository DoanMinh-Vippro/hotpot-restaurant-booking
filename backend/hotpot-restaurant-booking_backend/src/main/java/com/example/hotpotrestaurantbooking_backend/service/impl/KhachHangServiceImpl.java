// ===========================================
// KhachHangServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DatBanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangRequest;
import com.example.hotpotrestaurantbooking_backend.dto.KhachHangResponse;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {
    private final ModelMapper modelMapper;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    @Override
    public KhachHang getById(Integer id) {
        return khachHangRepository.findById(id).orElse(null);
    }

    @Override
    public KhachHang add(KhachHang khachHang) {
        return khachHangRepository.save(khachHang);
    }

    @Override
    public KhachHang update(Integer id, KhachHang khachHang) {

        KhachHang old = getById(id);

        old.setTenKhachHang(khachHang.getTenKhachHang());
        old.setSoDienThoai(khachHang.getSoDienThoai());
        old.setDiaChi(khachHang.getDiaChi());
        old.setEmail(khachHang.getEmail());

        return khachHangRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        khachHangRepository.deleteById(id);
    }

    @Override
    public List<KhachHangResponse> findAll() {
        return khachHangRepository.findAll().stream().map(khachHang -> {
            // 1. Khởi tạo đối tượng KhachHangResponse và map các trường cơ bản
            KhachHangResponse res = new KhachHangResponse();
            res.setId(khachHang.getIdKhachHang());
            res.setMaKhachHang(khachHang.getMaKhachHang());
            res.setTenKhachHang(khachHang.getTenKhachHang());
            res.setSoDienThoai(khachHang.getSoDienThoai());
            res.setEmail(khachHang.getEmail());
            res.setDiaChi(khachHang.getDiaChi());
            res.setGioiTinh(khachHang.getGioiTinh());
            res.setTrangThai(khachHang.getTrangThai());

            // 2. Map danh sách đơn Đặt Bàn lồng bên trong Khách Hàng
            if (khachHang.getDatBanList() != null) {

                // Sử dụng Khai báo Raw List để xóa sạch lỗi check kiểu Generic nghiêm ngặt của Java Compiler
                List listDatBanDto = khachHang.getDatBanList().stream().map(datBan -> {

                    // Khởi tạo đối tượng DTO nhận dữ liệu đặt bàn
                    // (Nếu trong dự án của bạn lớp này tên là DatBanDTO thì bạn đổi chữ DatBanResponse thành DatBanDTO nhé)
                    DatBanResponse dbDto = new DatBanResponse();

                    // Đảm bảo ép kiểu Integer chuẩn chỉ cho ID đặt bàn
                    if (datBan.getIdDatBan() != null) {
                        dbDto.setId(datBan.getIdDatBan());
                    }

                    dbDto.setNgayDat(datBan.getNgayDat());
                    dbDto.setGioDat(datBan.getGioDat());
                    dbDto.setSoNguoi(datBan.getSoNguoi());
                    dbDto.setSoTienCoc(datBan.getSoTienCoc());
                    dbDto.setThoiGianDenDuKien(datBan.getThoiGianDenDuKien());
                    dbDto.setGhiChu(datBan.getGhiChu());
                    dbDto.setSdtKhachHang(datBan.getSdtKhachHang());

                    // Lấy thông tin loại bàn từ liên kết Entity
                    if (datBan.getBan() != null) {
                        dbDto.setTenBan(datBan.getBan().getLoaiBan());
                    }

                    // Xử lý chuyển đổi Enum Trạng thái đặt bàn -> sang số Integer (1/0)
                    if (datBan.getTrangThai() != null) {
                        if (datBan.getTrangThai().name().equals("DA_XAC_NHAN")) {
                            dbDto.setTrangThai(1);
                        } else {
                            dbDto.setTrangThai(0);
                        }
                    }

                    // Xử lý chuyển đổi Enum Trạng thái cọc -> sang số Integer (1/0)
                    if (datBan.getTrangThaiCoc() != null) {
                        if (datBan.getTrangThaiCoc().name().equals("DA_COC")) {
                            dbDto.setTrangThaiCoc(1);
                        } else {
                            dbDto.setTrangThaiCoc(0);
                        }
                    }

                    // Ép Enum Phương thức thanh toán sang String bằng hàm .name()
                    if (datBan.getPhuongThucThanhToan() != null) {
                        dbDto.setPhuongThucThanhToan(datBan.getPhuongThucThanhToan().name());
                    } else {
                        dbDto.setPhuongThucThanhToan(null);
                    }

                    return dbDto;
                }).toList();

                // Gán danh sách đã map xong xuôi vào Khách hàng Response (Hết sạch lỗi gạch đỏ dòng này)
                res.setDatBanList(listDatBanDto);
            } else {
                res.setDatBanList(new java.util.ArrayList<>());
            }

            return res;
        }).toList();
    }

    @Override
    public KhachHangResponse findById(Integer id) {
        return khachHangRepository.findById(id)
                .map(khachHang -> modelMapper.map(khachHang, KhachHangResponse.class)).orElseThrow((()->new CustomResourceNotFoundException("Không có dữ liệu")));    }

    @Override
    public KhachHangResponse add(KhachHangRequest khachHangRequest) {
        KhachHang khachHang = modelMapper.map(khachHangRequest, KhachHang.class);
        khachHangRepository.save(khachHang);
        return modelMapper.map(khachHang, KhachHangResponse.class);    }

    @Override
    public KhachHangResponse update(KhachHangRequest khachHangRequest, Integer id) {
        return khachHangRepository.findById(id).map(khachHang -> {
            if(khachHangRequest.getMaKhachHang()!=null)khachHang.setMaKhachHang(khachHangRequest.getMaKhachHang());
            if(khachHangRequest.getTenKhachHang()!=null)khachHang.setTenKhachHang(khachHangRequest.getTenKhachHang());
            if(khachHangRequest.getGioiTinh() != null){khachHang.setGioiTinh(khachHangRequest.getGioiTinh());}
            if(khachHangRequest.getDiaChi()!=null)khachHang.setDiaChi(khachHangRequest.getDiaChi());
            if(khachHangRequest.getSoDienThoai()!=null)khachHang.setSoDienThoai(khachHangRequest.getSoDienThoai());
            if(khachHangRequest.getEmail()!=null)khachHang.setEmail(khachHangRequest.getEmail());
            if(khachHangRequest.getTrangThai() != null){khachHang.setTrangThai(khachHangRequest.getTrangThai());}
            khachHangRepository.save(khachHang);
            return modelMapper.map(khachHang, KhachHangResponse.class);
        }).orElseThrow(()->new CustomResourceNotFoundException("Không có dữ liệu"));     }

    @Override
    public List<KhachHang> search(String keyword) {
        return khachHangRepository
                . findByTenKhachHangContainingOrMaKhachHangContainingOrSoDienThoaiContainingOrEmailContainingOrTaiKhoan_MaTaiKhoanContaining(
                        keyword,
                        keyword,
                        keyword,
                        keyword,
                        keyword
                );    }
}