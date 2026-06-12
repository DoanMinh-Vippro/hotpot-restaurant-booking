// ===========================================
// KhuVucServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.BanResponse;
import com.example.hotpotrestaurantbooking_backend.dto.KhuVucRequest;
import com.example.hotpotrestaurantbooking_backend.dto.KhuVucResponse;
import com.example.hotpotrestaurantbooking_backend.entity.KhuVuc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.KhuVucRepository;
import com.example.hotpotrestaurantbooking_backend.service.KhuVucService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KhuVucServiceImpl implements KhuVucService {

    private final ModelMapper modelMapper;
    @Autowired
    private KhuVucRepository repository;

    @Override
    public List<KhuVuc> getAll() {
        return repository.findAll();
    }

    @Override
    public KhuVuc getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public KhuVuc add(KhuVuc khuVuc) {
        return repository.save(khuVuc);
    }

    @Override
    public KhuVuc update(Integer id, KhuVuc khuVuc) {

        KhuVuc old = getById(id);

        old.setMoTa(khuVuc.getMoTa());
        old.setTenKhuVuc(khuVuc.getTenKhuVuc());
        old.setTrangThai(khuVuc.getTrangThai());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public List<KhuVucResponse> findAll() {
        // Sử dụng hàm findAll() mặc định để tránh lỗi quét Class của DevTools
        return repository.findAll().stream().map(khuVuc -> {

            KhuVucResponse res = new KhuVucResponse();
            res.setId(khuVuc.getIdKhuVuc());
            res.setTenKhuVuc(khuVuc.getTenKhuVuc());
            res.setMoTa(khuVuc.getMoTa());
            res.setTrangThai(khuVuc.getTrangThai());

            // Duyệt mảng liên kết danh sách Bàn
            if (khuVuc.getBanList() != null) {
                List<BanResponse> listBanDto = khuVuc.getBanList().stream().map(ban -> {
                    BanResponse banDto = new BanResponse();

                    banDto.setId(ban.getIdBan());
                    banDto.setLoaiBan(ban.getLoaiBan());
                    banDto.setSoLuongBan(ban.getSoLuongBan());

                    // --- FIX LỖI GẠCH ĐỎ DÒNG 79 TẠI ĐÂY ---
                    // Chuyển đổi Enum TrangThaiBan sang dạng số nguyên (Integer) dựa trên vị trí Ordinal (0, 1, 2...)
                    if (ban.getTrangThai() != null) {
                        // Cách 1: Lấy theo vị trí số tự động của Enum (Bấm Alt+Shift+Enter như gợi ý Intellij)
                        banDto.setTrangThai(ban.getTrangThai().ordinal());

                        // Cách 2: Hoặc nếu nhóm bạn quy ước chuỗi chữ cụ thể thì Công dùng logic này:
                        // if (ban.getTrangThai().name().equals("TRONG")) banDto.setTrangThai(1);
                        // else banDto.setTrangThai(0);
                    }

                    // Gán mã số ID Khu vực trực tiếp sang DTO phẳng, bẻ gãy liên kết đệ quy
                    banDto.setKhuVucId(khuVuc.getIdKhuVuc());

                    return banDto;
                }).toList();

                res.setBanList(listBanDto);
            } else {
                res.setBanList(new java.util.ArrayList<>());
            }

            return res;
        }).toList();
    }

    @Override
    public KhuVucResponse findById(Integer id) {
        return repository.findById(id)
                .map(khuVuc -> modelMapper.map(khuVuc, KhuVucResponse.class))
                .orElseThrow((()->new CustomResourceNotFoundException("Không có dữ liệu")));       }

    @Override
    public KhuVucResponse add(KhuVucRequest khuVucRequest) {
        KhuVuc khuVuc = modelMapper.map(khuVucRequest, KhuVuc.class);
        repository.save(khuVuc);
        return modelMapper.map(khuVuc, KhuVucResponse.class);       }

    @Override
    public KhuVucResponse update(KhuVucRequest khuVucRequest, Integer id) {
        KhuVuc khuVuc = repository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khu vực"));

        khuVuc.setTenKhuVuc(khuVucRequest.getTenKhuVuc());
        khuVuc.setMoTa(khuVucRequest.getMoTa());
        khuVuc.setTrangThai(khuVucRequest.getTrangThai());

        khuVuc = repository.save(khuVuc);

        return modelMapper.map(khuVuc, KhuVucResponse.class);    }

    @Override
    public List<KhuVucResponse> search(String keyword) {
        return repository
                . findByTenKhuVuc(keyword)
                .stream()
                .map(khuVuc->modelMapper.map(khuVuc,KhuVucResponse.class))
                .toList();    }

    @Override
    public KhuVucResponse changeStatus(Integer id) {
        KhuVuc khuVuc = repository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy"));

        khuVuc.setTrangThai(khuVuc.getTrangThai() == 1 ? 0 : 1);

        repository.save(khuVuc);

        return modelMapper.map(khuVuc, KhuVucResponse.class);    }
}