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
            res.setIdKhuVuc(khuVuc.getIdKhuVuc());
            res.setTenKhuVuc(khuVuc.getTenKhuVuc());
            res.setMoTa(khuVuc.getMoTa());
            res.setTrangThai(khuVuc.getTrangThai());

            // Duyệt mảng liên kết danh sách Bàn
            if (khuVuc.getBanList() != null) {
                List<BanResponse> listBanDto = khuVuc.getBanList().stream().map(ban -> {
                    BanResponse banDto = new BanResponse();

                    banDto.setId(ban.getIdBan());
//                    banDto.setLoaiBan(ban.getLoaiBan());
//                    banDto.setSoLuongBan(ban.getSoLuongBan());

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
        // 1. Tìm khu vực cũ dưới DB lên
        KhuVuc khuVuc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));

        // 2. Cập nhật dữ liệu mới từ request vào entity
        khuVuc.setTenKhuVuc(khuVucRequest.getTenKhuVuc());
        khuVuc.setMoTa(khuVucRequest.getMoTa());
        khuVuc.setTrangThai(khuVucRequest.getTrangThai());

        // 3. Lưu vào Database
        KhuVuc khuVucDaUpdate = repository.save(khuVuc);

        // 4. TỰ MAP THỦ CÔNG: Tự tạo object Response để trả về, KHÔNG DÙNG ModelMapper nữa
        KhuVucResponse response = new KhuVucResponse();
        response.setId(khuVucDaUpdate.getIdKhuVuc());
        response.setTenKhuVuc(khuVucDaUpdate.getTenKhuVuc());
        response.setMoTa(khuVucDaUpdate.getMoTa());
        response.setTrangThai(khuVucDaUpdate.getTrangThai());

        // Chủ động gán danh sách bàn bằng null để triệt tiêu tận gốc lỗi DTO bên Bàn
        response.setBanList(null);

        // 5. Trả về kết quả hoàn toàn sạch lỗi
        return response;
    }


    @Override
    public List<KhuVucResponse> search(String keyword) {
        return repository
                . findByTenKhuVuc(keyword)
                .stream()
                .map(khuVuc->modelMapper.map(khuVuc,KhuVucResponse.class))
                .toList();    }

    // Bạn check xem tên hàm của bạn là gì nhé (ví dụ: changeStatus)
    @Override
    public KhuVucResponse changeStatus(Integer id) {
        // 1. Tìm khu vực cũ dưới DB
        KhuVuc khuVuc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));

        // 2. Logic đảo ngược trạng thái (Ví dụ: Đang 1 thành 0, đang 0 thành 1)
        // Hoặc nếu Frontend truyền status nào vào thì bạn gán trực tiếp bằng status đó
        if (khuVuc.getTrangThai() == 1) {
            khuVuc.setTrangThai(0); // Đổi thành Đang khóa
        } else {
            khuVuc.setTrangThai(1); // Đổi thành Hoạt động
        }

        // 3. Lưu vào Database
        KhuVuc khuVucDaUpdate = repository.save(khuVuc);

        // 4. XÓA MODELMAPPER - THAY BẰNG MAP THỦ CÔNG TẠI ĐÂY
        KhuVucResponse response = new KhuVucResponse();
        response.setId(khuVucDaUpdate.getIdKhuVuc() );
        response.setTenKhuVuc(khuVucDaUpdate.getTenKhuVuc());
        response.setMoTa(khuVucDaUpdate.getMoTa());
        response.setTrangThai(khuVucDaUpdate.getTrangThai());
        response.setBanList(null); // Triệt tiêu tận gốc lỗi Enum TrangThaiBan

        return response;
    }
}